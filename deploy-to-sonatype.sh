#!/bin/bash
if [[ "${TRAVIS_BRANCH}" == 'master' ]] ; then
    ./generate-maven-settings.sh
    mvn deploy -s ./target/maven-settings.xml
fi