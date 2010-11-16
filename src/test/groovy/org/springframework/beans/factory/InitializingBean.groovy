package org.springframework.beans.factory

/**
 * @author andre.steingress@gmail.com
 */
public interface InitializingBean {

  void afterPropertiesSet() throws Exception
}