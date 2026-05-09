ARG ELASTICSEARCH_VERSION=8.18.8
FROM elasticsearch:${ELASTICSEARCH_VERSION}

ARG ELASTICSEARCH_VERSION
# batch： 自动确认所有确认提示
RUN elasticsearch-plugin install --batch https://get.infini.cloud/elasticsearch/analysis-ik/${ELASTICSEARCH_VERSION}
