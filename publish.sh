path=~/projects/EMenuSystems/dev
target=~/projects/EMenuSystems/production

echo "cp $path $target ..."
cp -r "$path" "$target"

echo "scp .zip ..."
sshpass -p '1234' scp target/VertxService-1.0-SNAPSHOT-mod.zip ubuntu@192.168.0.109:emenu/black

echo "sed ip ..."
sed -i 's/10.0.0.67:8877/192.168.0.109:8877/g' "$target"/js/final.js

echo "sed live ..."
sed -i 's/http:\/\/10.0.0.67:35729\/livereload.js//g' "$target"/index.html

echo "scp $target"
sshpass -p '1234' scp -r "$target" ubuntu@192.168.0.109:emenu/black/WebManager
