#!/bin/bash

# Script to create a new release
# ------------------------------

# exit on error
set -e

# make sure we're on master
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" != "master" ]]; then
  echo "You're on branch '$BRANCH', you should be on 'master'."
  exit
fi

# make sure CHANGELOG.md has been updated
read -r -p "Has CHANGELOG.md been updated? (Y/n) " CONFIRM
case "$CONFIRM" in
n | N) exit ;;
esac

read -r -p "New version (x.y.z): " VERSION

mvn versions:set -DgenerateBackupPoms=false -DnewVersion="$VERSION"
git add .
git commit -am "release $VERSION"
git tag "$VERSION"

read -r -p "Push master and tag $VERSION? (y/N) " CONFIRM
case "$CONFIRM" in
y | Y) git push --atomic origin master "$VERSION" ;;
esac
