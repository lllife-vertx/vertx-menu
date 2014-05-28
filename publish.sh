system_source=~/projects/EMenuSystems/dev
system_production=~/projects/EMenuSystems/production

report_source=~/projects/EMenuReport/dev
report_production=~/projects/EMenuReport/production
host="ubuntu@192.168.0.106"

mvn install

echo "remove $system_production"
echo "remove $report_production"
rm -rf "$system_production"
rm -rf "$report_production"

echo "copy $system_source $system_production"
echo "copy $report_source $report_production"
cp -fr "$system_source" "$system_production"
cp -fr "$report_source" "$report_production"

echo "scp .zip ..."
sshpass -p '1234' scp target/VertxService-1.0-SNAPSHOT-mod.zip "$host:emenu/black"

#sed -i 's/10.0.0.67:8877/192.168.0.106:8877/g' "$system_production"/js/final.js
#sed -i 's/10.0.0.67:8877/192.168.0.106:8877/g' "$report_production"/js/final.js

sed -i 's/10.0.0.67:8877/emenu.ecmxpert.com:8877/g' "$system_production"/js/final.js
sed -i 's/10.0.0.67:8877/emenu.ecmxpert.com:8877/g' "$report_production"/js/final.js

sed -i 's/http:\/\/10.0.0.67:35729\/livereload.js//g' "$system_production"/index.html
sed -i 's/http:\/\/10.0.0.67:8888\/livereload.js//g' "$system_production"/index.html


echo "clear..."
sshpass -p '1234' ssh "$host" 'killall node; killall java'
sshpass -p '1234' ssh "$host" 'rm -rf ~/emenu/black/WebManager' 
sshpass -p '1234' ssh "$host" 'rm -rf ~/emenu/black/WebReport' 

echo "scp $target"
sshpass -p '1234' scp -r "$system_production" "$host":emenu/black/WebManager
sshpass -p '1234' scp -r "$report_production" "$host":emenu/black/WebReport
sshpass -p '1234' ssh "$host" 'sh ~/emene/black/startup &'
