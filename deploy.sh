#!/usr/bin/env bash
# 데일리 브리핑 대시보드 배포 스크립트
#   bash deploy.sh            → 커밋 → push → Pages 활성화 → HTTP 200 확인
#   bash deploy.sh --status   → 현재 상태만 확인
set -uo pipefail

OWNER="clghks86"
REPO="daily-briefing"
BRANCH="main"
URL="https://${OWNER}.github.io/${REPO}/"
API_DIR="repos/${OWNER}/${REPO}"
cd "$(dirname "$0")" || exit 1

say() { printf '\n\033[1m%s\033[0m\n' "$*"; }
ok()  { printf '  \033[32m✓\033[0m %s\n' "$*"; }
err() { printf '  \033[31m✗\033[0m %s\n' "$*"; }

check_account() {
  local login
  login=$(gh api user --jq .login 2>/dev/null)
  [ "$login" = "$OWNER" ] || { err "gh 로그인이 ${OWNER}가 아닙니다 (${login:-없음})"; return 1; }
  ok "GitHub 계정: ${login}"
}

repo_exists() {
  gh api "${API_DIR}" >/dev/null 2>&1
}

show_status() {
  say "저장소 / Pages 상태"
  if repo_exists; then
    ok "저장소 있음: https://github.com/${OWNER}/${REPO}"
    gh api "${API_DIR}/pages" --jq '"  Pages: " + .status + " (" + .html_url + ")"' 2>/dev/null \
      || err "Pages 비활성"
    printf '  라이브 HTTP: %s\n' "$(curl -s -o /dev/null -w '%{http_code}' "$URL")"
  else
    err "저장소 없음: https://github.com/${OWNER}/${REPO}"
  fi
}

if [ "${1:-}" = "--status" ]; then
  check_account
  show_status
  exit 0
fi

say "1) 계정 확인"
check_account || exit 1

say "2) 저장소 확인"
if ! repo_exists; then
  cat <<EOF
  ✗ 저장소가 아직 없습니다: https://github.com/${OWNER}/${REPO}

  GitHub App 권한으로는 저장소를 새로 만들 수 없습니다 (createRepository 403).
  30초면 끝납니다:
    1. https://github.com/new 접속
    2. Repository name: ${REPO}
    3. Public 선택
    4. README / .gitignore / license 추가 없이 [Create repository]

  만든 뒤 다시 'bash deploy.sh' 를 실행하면 push·Pages 활성화·검증까지 자동으로 끝납니다.
EOF
  exit 1
fi
ok "저장소 있음"

say "3) 시크릿 검사"
if grep -rIlE --exclude-dir=.git 'sb_secret_[A-Za-z0-9_-]{10,}' . 2>/dev/null | grep -q .; then
  err "secret 키로 보이는 문자열이 있습니다. 커밋을 중단합니다:"
  grep -rInE --exclude-dir=.git 'sb_secret_[A-Za-z0-9_-]{10,}' . | head
  exit 1
fi
ok "secret 키 없음 (publishable 키만 사용)"

say "4) 커밋 & push"
if [ ! -d .git ]; then
  git init -q
  git checkout -q -b "$BRANCH" 2>/dev/null || git symbolic-ref HEAD "refs/heads/${BRANCH}"
  ok "git 저장소 초기화"
fi
git add -A
if git diff --cached --quiet; then
  ok "변경 없음 — 커밋 생략"
else
  git -c user.name="${OWNER}" -c user.email="${OWNER}@users.noreply.github.com" \
      commit -q -m "데일리 브리핑 대시보드 배포 ($(date '+%Y-%m-%d %H:%M KST'))"
  ok "커밋: $(git log -1 --pretty=%h)"
fi
git remote get-url origin >/dev/null 2>&1 \
  && git remote set-url origin "https://github.com/${OWNER}/${REPO}.git" \
  || git remote add origin "https://github.com/${OWNER}/${REPO}.git"
if git push -u origin "$BRANCH" 2>&1 | tail -3; then
  ok "push 완료 ($(git rev-parse --short HEAD))"
else
  err "push 실패 — App 설치 범위에 이 저장소가 들어있는지 확인 필요"
  exit 1
fi

say "5) GitHub Pages 활성화 (main / root)"
code=$(gh api -X POST "${API_DIR}/pages" \
  -f 'source[branch]=main' -f 'source[path]=/' 2>/dev/null; echo)
code_status=$(gh api -X POST "${API_DIR}/pages" -f 'source[branch]=main' -f 'source[path]=/' >/dev/null 2>&1; echo $?)
if [ "$code_status" = "0" ]; then
  ok "Pages 활성화됨"
else
  # 이미 켜져 있으면 409
  if gh api "${API_DIR}/pages" >/dev/null 2>&1; then
    ok "Pages 이미 활성화됨"
  else
    err "Pages 활성화 실패 — 저장소 Settings > Pages에서 직접 켜야 할 수 있습니다"
  fi
fi

say "6) 라이브 확인 (최대 180초)"
for i in $(seq 1 60); do
  status=$(curl -s -o /dev/null -w '%{http_code}' "$URL")
  if [ "$status" = "200" ]; then
    ok "HTTP 200 — $URL"
    break
  fi
  printf '  … %s (%s초)\n' "$status" "$((i * 3))"
  sleep 3
done

if [ "${status:-}" != "200" ]; then
  err "아직 200이 아닙니다. 첫 배포는 1~3분 걸릴 수 있습니다. 'bash deploy.sh --status'로 재확인하세요."
  exit 1
fi

say "7) 서빙된 파일 == 로컬 파일 확인"
local_hash=$(python3 -c "import hashlib,sys;print(hashlib.sha256(open('index.html','rb').read()).hexdigest())")
live_hash=$(curl -s "$URL" | python3 -c "import hashlib,sys;print(hashlib.sha256(sys.stdin.buffer.read()).hexdigest())")
if [ "$local_hash" = "$live_hash" ]; then
  ok "동일 (${local_hash:0:12}…) — 최신 커밋이 서빙 중"
else
  err "해시 불일치: local ${local_hash:0:12}… / live ${live_hash:0:12}… (Pages 빌드 대기 중일 수 있음)"
fi
echo
ok "완료: $URL"
