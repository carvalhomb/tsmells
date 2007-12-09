TMPFILE=$(mktemp) && \
cat $TSMELLS/src/initJavaTestEntities.rml $TSMELLS/src/SensitiveEquality.rml > $TMPFILE && \
cat rsf/ToStringTComMulti.rsf | crocopat $TMPFILE && \
rm -rf $TMPFILE &> /dev/null;
