#/bin/bash
userPrinc=$1
extendDir=hdp39@BMSOFT.COM
keyDir=/opt/keystore/
/usr/sbin/kadmin.local << EOF
addprinc ${userPrinc}/$extendDir
${userPrinc}
${userPrinc}
ktadd -k $keyDir${userPrinc}.keytab  ${userPrinc}/$extendDir
EOF
