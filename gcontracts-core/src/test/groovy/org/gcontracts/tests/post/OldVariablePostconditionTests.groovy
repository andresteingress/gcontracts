package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * <tt>old</tt> variables tests for postconditions.
 *
 * @see org.gcontracts.annotations.Ensures
 *
 * @author ast
 */

class OldVariablePostconditionTests extends BaseTestClass {

  def templateSourceCode = '''
package tests

import org.gcontracts.annotations.*

class OldVariable {

  private $type someVariable

  def OldVariable(final $type other)  {
    someVariable = other
  }

  @Ensures({ old -> old.someVariable != null && old.someVariable != someVariable })
  void setVariable(final $type other)  {
    this.someVariable = other
  }
}
'''

  @Test void big_decimal()  {

    def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: BigDecimal.class.getName()]), new BigDecimal(0))
    instance.setVariable new BigDecimal(1)
  }


  @Test void big_integer()  {
    def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: BigInteger.class.getName()]), new BigInteger(0))
    instance.setVariable new BigInteger(1)
  }

  @Test void string()  {
    def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: String.class.getName()]), ' ')
    instance.setVariable 'test'
  }

  @Test void integer()  {
    def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Integer.class.getName()]), new Integer(0))
    instance.setVariable new Integer(1)
  }

  @Test void test_float()  {
    def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Float.class.getName()]), new Float(0))
    instance.setVariable new Float(1)
  }

  @Test void test_calendar_date()  {
    def now = Calendar.getInstance()
    def not_now = Calendar.getInstance()
    not_now.add(Calendar.DAY_OF_YEAR, 1)

    def instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Calendar.class.getName()]), now)
    instance.setVariable not_now

    def date_now = now.getTime()
    def date_not_now = not_now.getTime()

    instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: Date.class.getName()]), date_now)
    instance.setVariable date_not_now

    def sql_date_now = new java.sql.Date(date_now.getTime())
    def sql_date_not_now = new java.sql.Date(date_not_now.getTime())

    instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: java.sql.Date.class.getName()]), sql_date_now)
    instance.setVariable sql_date_not_now

    def ts_now = new java.sql.Timestamp(date_now.getTime())
    def ts_not_now = new java.sql.Timestamp(date_not_now.getTime())

    instance = create_instance_of(createSourceCodeForTemplate(templateSourceCode, [type: java.sql.Timestamp.class.getName()]), ts_now)
    instance.setVariable ts_not_now

    //instance = create_instance_of(createSourceCodeForTemplate(dynamic_constructor_class_code, [type: GString.class.getName()]), "${''}")
    //instance.setVariable "${'test' + 1}"
  }

  @Test void generate_old_variables_for_super_class()  {

    def baseClassSource = '''
    package tests

    import org.gcontracts.annotations.*

    class Account
{
    protected BigDecimal balance

    def Account( BigDecimal amount = 0.0 )
    {
        balance = amount
    }

    void deposit( BigDecimal amount )
    {
        balance += amount
    }

    @Requires({ amount >= 0.0 })
    BigDecimal withdraw( BigDecimal amount )
    {
        if (balance < amount) return 0.0

        balance -= amount
        return amount
    }

    BigDecimal getBalance()
    {
        return balance
    }
}
'''

    def descendantClassSource = '''
    package tests

    import org.gcontracts.annotations.*

    class BetterAccount extends Account {

    @Ensures({ balance == old.balance - (amount * 0.5) })
    BigDecimal withdraw( BigDecimal amount )
    {
        if (balance < amount) return 0.0

        balance -= amount * 0.5
        return amount
    }
}

'''

      add_class_to_classpath baseClassSource

      def betterAccount = create_instance_of(descendantClassSource)
      betterAccount.deposit (30.0)
      betterAccount.withdraw (10.0)

  }
}