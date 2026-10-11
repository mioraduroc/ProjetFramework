#!/bin/bash
set -e

# --- CONFIGURATION DES CHEMINS ---
BASE_DIR="/home/miora/Documents/S5/PROJET_FRAMEWORK"
FRAMEWORK_DIR="$BASE_DIR/Git"
TEST_APP_DIR="$BASE_DIR/TestApplication"
TOMCAT_HOME="/home/miora/Applications/tomcat"

echo "=== 1. Compilation du Framework ($FRAMEWORK_DIR) ==="
cd "$FRAMEWORK_DIR"

APP_NAME="framework"
SRC_DIR="src/main/java"
BUILD_DIR="build"
LIB_DIR="lib"

rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes $BUILD_DIR/lib

# Compilation du Framework avec le flag -parameters
find $SRC_DIR -name "*.java" > sources.txt
javac -parameters -cp ".:$LIB_DIR/*" -d $BUILD_DIR/classes @sources.txt
rm sources.txt

cd $BUILD_DIR || exit
jar -cvf $APP_NAME.jar -C classes .
cd "$FRAMEWORK_DIR"

# Copie du framework.jar dans le WEB-INF/lib de TestApplication
mkdir -p "$TEST_APP_DIR/src/main/webapp/WEB-INF/lib"
cp -f $BUILD_DIR/$APP_NAME.jar "$TEST_APP_DIR/src/main/webapp/WEB-INF/lib/"

# Copie des dépendances (sans servlet-api)
for j in $LIB_DIR/*.jar; do
  if [ -f "$j" ]; then
    base=$(basename "$j")
    if [[ "$base" != "servlet-api.jar" && "$base" != "jakarta.servlet-api.jar" ]]; then
      cp -f "$j" "$TEST_APP_DIR/src/main/webapp/WEB-INF/lib/"
    fi
  fi
done
echo "--> Framework empaqueté et copié dans TestApplication."


echo "=== 2. Compilation de TestApplication ($TEST_APP_DIR) ==="
cd "$TEST_APP_DIR"

TEST_SRC="src/main/java"
TEST_WEB="src/main/webapp"
TEST_BUILD="build"
SERVLET_API_JAR="$TEST_WEB/WEB-INF/lib/*"

rm -rf $TEST_BUILD
mkdir -p $TEST_BUILD/WEB-INF/classes $TEST_BUILD/WEB-INF/lib

# Compilation des contrôleurs de l'application de test avec -parameters
find $TEST_SRC -name "*.java" > sources.txt
javac -parameters -cp "$SERVLET_API_JAR" -d $TEST_BUILD/WEB-INF/classes @sources.txt
rm sources.txt

# Copie des ressources Web (JSP, web.xml, lib)
cp -r $TEST_WEB/* $TEST_BUILD/

cd $TEST_BUILD || exit
jar -cvf testApplication.war *
cd "$TEST_APP_DIR"


echo "=== 3. Purge du cache Tomcat & Redéploiement ==="
# Arrêt propre de Tomcat s'il tourne
$TOMCAT_HOME/bin/shutdown.sh 2>/dev/null || true

# Suppression stricte des résidus décompressés et du cache Tomcat
rm -rf $TOMCAT_HOME/webapps/testApplication
rm -rf $TOMCAT_HOME/webapps/testApplication.war
rm -rf $TOMCAT_HOME/work/Catalina/localhost/testApplication

# Copie du WAR tout neuf dans Tomcat
cp -f $TEST_BUILD/testApplication.war $TOMCAT_HOME/webapps/

# Démarrage de Tomcat
$TOMCAT_HOME/bin/startup.sh

echo "=== Tout est compilé et déployé avec succès ! ==="