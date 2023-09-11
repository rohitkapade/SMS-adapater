#!/bin/sh
echo "Setting hooks"
git config core.hooksPath .githooks
find .git/hooks -type l -exec rm {} \;
find .githooks -type f -exec ln -sf ../../{} .git/hooks/ \;

