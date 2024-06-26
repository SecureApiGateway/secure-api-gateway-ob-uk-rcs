name := securebanking-openbanking-uk-rcs
repo := europe-west4-docker.pkg.dev/sbat-gcr-develop/sapig-docker-artifact
tag  := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
helm_repo := forgerock-helm/secure-api-gateway/securebanking-openbanking-uk-rcs/

clean:
	mvn clean

install:
	mvn -U install

verify: clean
	mvn verify

docker: install
	mvn dockerfile:build dockerfile:push -DskipTests -DskipITs -Dtag=${tag} \
	  -DgcrRepo=${repo} --file secure-api-gateway-ob-uk-rcs-server/pom.xml

package_helm:
ifndef version
	$(error A version must be supplied, Eg. make helm version=1.0.0)
endif
	helm dependency update _infra/helm/${name}
	helm template _infra/helm/${name}
	helm package _infra/helm/${name} --version ${version} --app-version ${version}

publish_helm:
ifndef version
	$(error A version must be supplied, Eg. make helm version=1.0.0)
endif
	jf rt upload  ./*-${version}.tgz ${helm_repo}

dev: clean
	mvn install package -DskipTests -DskipITs -Dtag=latest -DgcrRepo=${repo} \
	  --file secure-api-gateway-ob-uk-rcs-server/pom.xml

version:
	@echo $(tag)