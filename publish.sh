path=~/projects/EMenuSystems/dev
target=~/projects/EMenuSystems/production

mvn install

echo "remove $target..."
rm -rf "$target"

echo "cp -fr $path $target ..."
cp -fr "$path" "$target"

echo "scp .zip ..."
sshpass -p '1234' scp target/VertxService-1.0-SNAPSHOT-mod.zip ubuntu@192.168.0.109:emenu/black

echo "sed ip ..."
sed -i 's/10.0.0.67:8877/192.168.0.109:8877/g' "$target"/js/final.js

echo "sed live ..."
sed -i 's/http:\/\/10.0.0.67:35729\/livereload.js//g' "$target"/index.html

echo "clear..."
sshpass -p '1234' ssh ubuntu@192.168.0.109 'killall node; killall java; rm -rf ~/emenu/black/WebManager' 

echo "scp $target"
sshpass -p '1234' scp -r "$target" ubuntu@192.168.0.109:emenu/black/WebManager

sshpass -p '1234' ssh ubuntu@192.168.0.109 'sh ~/emene/black/startup &'
