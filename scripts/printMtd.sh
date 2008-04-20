#!/bin/bash

if [[ "$1" == "" || "$2" == "" ]]
then
    echo "usage: $0 <quoted-method-signature> <rsf-file>"
    exit -1
fi

MTD=$(echo "$1" | sed -e 's/(/\\(/' -e 's/)/\\)/' -e 's/\*/\\\*/g') # escape the '(' and ')' for regexp
RSF=$2

MTD_ID_DECL=$(grep -P "^(Method|Function)	.*$MTD" $RSF | cut -f2)
# DefinitionForDeclaration is really just +1 ... mostly
MTD_ID=$(($MTD_ID_DECL + 1))

ENT_NAME_TMP="entities.tmp"    # names of invoked/accessed entities
SRC_LINE_TMP="sourcelines.tmp" # corresponding source line

# grep the invocations of MTD
INV_TMP="invocations.tmp"
grep -P "^Invokes	[[:digit:]].*	$MTD_ID	[[:digit:]].*" $RSF > $INV_TMP

# grab the invokee signatures
cat $INV_TMP |\
cut -f4 |\
xargs -P 10 -I INV_ID grep -P "^(Method|Function)	INV_ID	" $RSF |\
cut -f3 > $ENT_NAME_TMP

# get the source lines of these invocations
cat $INV_TMP |\
cut -f2 |\
xargs -P 10 -I INV_ID grep -P "^LineNo	INV_ID	" $RSF |\
cut -f4 > $SRC_LINE_TMP

# fetch the accesses of MTD
ACC_TMP="accesses.tmp"
grep -P "^Accesses	[[:digit:]].*	$MTD_ID	[[:digit:]].*" $RSF > $ACC_TMP

# append the accessed attribute name
cat $ACC_TMP |\
cut -f4 |\
xargs -P 10 -I ATTR_ID grep -P "^Attribute	ATTR_ID	" $RSF |\
cut -f3 >> $ENT_NAME_TMP

# determine the source lines these attributes are accessed on
cat $ACC_TMP |\
cut -f2 |\
xargs -P 10 -I ACC_ID grep -P "^LineNo	ACC_ID	" $RSF |\
cut -f4 >> $SRC_LINE_TMP

echo $1
# concatenate linenumbers and methods
paste $SRC_LINE_TMP $ENT_NAME_TMP | sort -n | sed -e 's/	/: /'
echo "#invo+acc : "$(cat $INV_TMP $ACC_TMP | wc -l)
echo "#loc      : "$(grep -P "Measurement	[[:digit:]].*	$MTD_ID_DECL	\"LOC\"" $RSF | cut -f5)
rm -f $ENT_NAME_TMP $SRC_LINE_TMP $INV_TMP $ACC_TMP
