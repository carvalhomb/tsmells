TMPFILE=$(mktemp) && \
cat $TSMELLS/src/initJavaTestEntities.rml $TSMELLS/src/SensitiveEquality.rml > $TMPFILE && \
cat rsf/ToStringSingleTHelp.rsf | crocopat $TMPFILE && \
rm -rf $TMPFILE &> /dev/null;
