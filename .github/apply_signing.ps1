echo $Env:SIGNING_KEY | base64 -d > ./key.gpg
echo "signing.keyId=${Env:SIGNING_KEY_ID}
signing.password=${Env:SIGNING_PASSWORD}
signing.secretKeyRingFile=./key.gpg
ossrhUsername=${Env:OSSRH_USERNAME}
ossrhPassword=${Env:OSSRH_PASSWORD}" >publish.properties