javarsf2tsmell.sh "*.rsf" -DDUPLICATED_CODE -DDUPLICATED_CODE_PYGEN=TSMELLS/src/dump/DuplicatedCode.py -DDUPLICATED_CODE_TRESHOLD=6 && ../waitForFinish.sh && cat DUPLI_TMP
