package org.barrelorgandiscovery.optimizers.punch;

import com.l2fprod.common.beans.BaseBeanInfo;

public class NoReturnPunchConverterOptimizerParametersBeanInfo extends BaseBeanInfo {

  public NoReturnPunchConverterOptimizerParametersBeanInfo() {

    super(NoReturnPunchConverterOptimizerParameters.class);

    PunchConverterOptimizerParametersBeanInfo.addProperties(this);
  }
}
