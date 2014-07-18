#!/bin/sh

git add .
git commit -m "[update docs]"
cd ..
git subtree push --prefix docs/ origin gh-pages
