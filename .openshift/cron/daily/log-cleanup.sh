if [ $OPENSHIFT_JBOSSAS_LOG_DIR ]; then
	rm -rf $OPENSHIFT_JBOSSAS_LOG_DIR/*.log.*
fi

if [ $OPENSHIFT_JBOSSEAP_LOG_DIR ]; then
	rm -rf $OPENSHIFT_JBOSSEAP_LOG_DIR/*.log.*
fi