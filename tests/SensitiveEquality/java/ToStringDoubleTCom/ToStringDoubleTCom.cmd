TMPFILE=$(mktemp) && \
cat $TSMELLS/src/initJavaTestEntities.rml $TSMELLS/src/SensitiveEquality.rml > $TMPFILE && \
cat rsf/ToStringDoubleTCom.rsf | crocopat $TMPFILE && \
rm -rf $TMPFILE &> /dev/null;
