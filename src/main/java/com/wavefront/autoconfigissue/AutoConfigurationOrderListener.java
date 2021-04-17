package com.wavefront.autoconfigissue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationImportEvent;
import org.springframework.boot.autoconfigure.AutoConfigurationImportListener;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

// copied from ttddyy's comment here https://github.com/spring-projects/spring-boot/issues/20732#issuecomment-687544812
public class AutoConfigurationOrderListener
    implements BeanFactoryAware, ResourceLoaderAware, AutoConfigurationImportListener {

  private final Logger logger = LoggerFactory.getLogger(AutoConfigurationOrderListener.class);

  private static final String SORTER_CLASS = "org.springframework.boot.autoconfigure.AutoConfigurationSorter";

  private ConfigurableListableBeanFactory beanFactory;

  private ResourceLoader resourceLoader;

  @Override
  public void onAutoConfigurationImportEvent(AutoConfigurationImportEvent event) {
    List<String> configs = event.getCandidateConfigurations();
    List<String> sorted;
    try {
      sorted = sort(configs);
    }
    catch (Exception ex) {
      logger.error("Failed to sort auto configuration classes", ex);
      return; // do not proceed
    }

    // If we want to follow how "ConditionEvaluationReport" is logged, log them at
    // "ContextRefreshedEvent" and "ApplicationFailedEvent" with implementing
    // "GenericApplicationListener" (see "ConditionEvaluationReportLoggingListener").
    // Then, register to "spring.factories" as "ApplicationListener".
    logAutoConfigurationOrder(sorted);
  }

  private void logAutoConfigurationOrder(List<String> configs) {
    StringBuilder sb = getLogMessage(configs, "AUTO CONFIGURATION ORDER REPORT");
    logger.info(sb.toString());
  }

  private StringBuilder getLogMessage(List<String> configs, String title) {
    StringBuilder message = new StringBuilder();
    message.append(String.format("%n%n%n"));
    StringBuilder separator = new StringBuilder();
    for (int i = 0; i < title.length(); i++) {
      separator.append("=");
    }
    message.append(String.format("%s%n", separator));
    message.append(String.format("%s%n", title));
    message.append(String.format("%s%n%n%n", separator));
    for (int i = 0; i < configs.size(); i++) {
      message.append(String.format("%3d - %s%n", i + 1, configs.get(i)));
    }
    message.append(String.format("%n%n"));
    return message;
  }

  // Equivalent to
  // "org.springframework.boot.autoconfigure.AutoConfigurationImportSelector.AutoConfigurationGroup.sortAutoConfigurations"
  @SuppressWarnings("unchecked")
  private List<String> sort(List<String> configs) throws Exception {

    Class<?> clazz = ClassUtils.resolveClassName(SORTER_CLASS,
        AutoConfigurationOrderListener.class.getClassLoader());
    Method method = ReflectionUtils.findMethod(clazz, "getInPriorityOrder", Collection.class);
    ReflectionUtils.makeAccessible(method);

    Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz, MetadataReaderFactory.class,
        AutoConfigurationMetadata.class);
    Object[] args = new Object[] { getMetadataReaderFactory(), getAutoConfigurationMetadata() };

    Object sorter = BeanUtils.instantiateClass(constructor, args);

    List<String> sorted = (List<String>) ReflectionUtils.invokeMethod(method, sorter, configs);
    return sorted;
  }

  // Equivalent to
  // "org.springframework.boot.autoconfigure.AutoConfigurationImportSelector.AutoConfigurationGroup.getMetadataReaderFactory"
  private MetadataReaderFactory getMetadataReaderFactory() {
    String beanName = "org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory";
    try {
      return this.beanFactory.getBean(beanName, MetadataReaderFactory.class);
    }
    catch (NoSuchBeanDefinitionException ex) {
      return new CachingMetadataReaderFactory(this.resourceLoader);
    }
  }

  // Equivalent to
  // "org.springframework.boot.autoconfigure.AutoConfigurationImportSelector.AutoConfigurationGroup.getAutoConfigurationMetadata"
  private AutoConfigurationMetadata getAutoConfigurationMetadata() {
    String loaderClass = "org.springframework.boot.autoconfigure.AutoConfigurationMetadataLoader";
    Class<?> clazz = ClassUtils.resolveClassName(loaderClass,
        AutoConfigurationOrderListener.class.getClassLoader());
    Method method = ReflectionUtils.findMethod(clazz, "loadMetadata", ClassLoader.class);
    ReflectionUtils.makeAccessible(method);
    AutoConfigurationMetadata metadata = (AutoConfigurationMetadata) ReflectionUtils.invokeMethod(method, null,
        AutoConfigurationOrderListener.class.getClassLoader());
    return metadata;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
    this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

}
