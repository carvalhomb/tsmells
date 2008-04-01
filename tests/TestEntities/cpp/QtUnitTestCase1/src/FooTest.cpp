#include <QtTest/QtTest>

class FooTest : public QObject
{
    Q_OBJECT

private slots:
    void testFoo();

};

void FooTest::testFoo()
{
}

QTEST_MAIN(FooTest)

#include "FooTest.moc"

