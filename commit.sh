#!/bin/sh

cd /home/akshayk/Akshay/Android/WakeUp

echo -n "Enter a commit message > "
read commitMessage
if [ -z "$commitMessage" ]; then
	echo "Invalid message"
	./commit.sh
	exit
fi

echo ""
echo ""
echo ---------------------------------------------------------
echo "git add ."
echo ---------------------------------------------------------
echo ""
echo ""

git add .

echo ""
echo ""
echo ---------------------------------------------------------
echo "git commit -m $commitMessage"
echo ---------------------------------------------------------
echo ""
echo ""

git commit -m "$commitMessage"

echo ""
echo ""
echo ---------------------------------------------------------
echo "Pushing to Bitbucket"
echo ---------------------------------------------------------
echo ""
echo ""

git push origin master

echo ""
echo ""
echo ---------------------------------------------------------
echo "Pushing to Github"
echo ---------------------------------------------------------
echo ""
echo ""

git push origin_github master
