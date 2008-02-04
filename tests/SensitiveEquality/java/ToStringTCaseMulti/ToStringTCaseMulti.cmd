TMPFILE=$(mktemp) && \
cat $TSMELLS/src/initJavaTestEntities.rml $TSMELLS/src/SensitiveEquality.rml > $TMPFILE && \
cat rsf/ToStringTCaseMulti.rsf | crocopat $TMPFILE 2>/dev/null && \
rm -rf $TMPFILE &> /dev/null;
