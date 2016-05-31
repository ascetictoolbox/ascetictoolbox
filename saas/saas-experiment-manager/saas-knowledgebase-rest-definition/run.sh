

swagger-codegen generate -i ascetic-swagger.yaml \
  -l java \
  -o ../saas-knowledgebase-rest-client \
  -c config-swagger-client.json

swagger-codegen generate -i ascetic-swagger.yaml \
  -l nodejs \
  -o ../saas-knowledgebase-rest-server-template \
  -c config-swagger-server.json

cp -f ../saas-knowledgebase-rest-server-template/api/swagger.yaml ../saas-knowledgebase-rest-server/api/swagger.yaml
cp -f ../saas-knowledgebase-rest-server-template/controllers/Default.js ../saas-knowledgebase-rest-server/controllers/Default.js
cp -f ../saas-knowledgebase-rest-server-template/controllers/DefaultService.js ../saas-knowledgebase-rest-server/controllers/DefaultService_template.js
rm -rf ../saas-knowledgebase-rest-server-template
