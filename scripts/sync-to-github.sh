#!/usr/bin/env bash
set -euo pipefail

REMOTE_URL=${1:-"https://github.com/KongYiji1994/BankCore1.git"}
BRANCH=${2:-"work"}

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "This script must be run inside a git repository." >&2
  exit 1
fi

echo "Using remote: ${REMOTE_URL}"
if ! git remote get-url origin >/dev/null 2>&1; then
  git remote add origin "${REMOTE_URL}"
  echo "Remote 'origin' added."
else
  echo "Remote 'origin' already configured as $(git remote get-url origin)."
fi

echo "Fetching latest refs from origin to avoid fast-forward issues..."
git fetch origin || echo "Warning: fetch failed (likely due to auth); push may still prompt for credentials." >&2

echo "Pushing branch '${BRANCH}' to origin..."
git push -u origin "${BRANCH}"
