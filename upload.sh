# upload.sh by zhouhaoh
function upload()
{
curl -F "file=@$1" -F '_api_key=db181f42144123126a11c7f5bf68' -F "buildUpdateDescription=${SCM_CHANGELOG}" https://www.pgyer.com/apiv2/app/upload
}
cd apk/
for file in `ls`
do
 if [[ $file =~ \.apk$ ]] ;then
 	echo "-----------------"
 	#stat -f $file
    upload $file
 fi
done