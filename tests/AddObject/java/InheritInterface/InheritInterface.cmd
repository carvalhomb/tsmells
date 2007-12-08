rm -rf src/dbdump src/*.proj || true && \
mkdir src/dbdump && \
cd src && \
${SN_HOME}/snavigator --batchmode --create > /dev/null && \
cd .. && \
bash $FETCH/src/snavtofamix/snav_dbdumps.sh src src src/dbdump > /dev/null && \
$TSMELLS/scripts/AddObject/fixObject.sh src/dbdump && \
grep "Object" src/dbdump/src.inheritance || true