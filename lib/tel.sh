#!/bin/bash

# Version de Spring (compatible Jakarta)
SPRING_VERSION="6.1.8"

# Dossier de destination
LIB_DIR="spring-lib"

mkdir -p $LIB_DIR
cd $LIB_DIR

echo "Téléchargement de Spring version $SPRING_VERSION..."

# Liste des modules nécessaires
BASE_URL="https://repo1.maven.org/maven2/org/springframework"

FILES=(
"spring-core"
"spring-beans"
"spring-context"
"spring-web"
"spring-jcl"
)

for file in "${FILES[@]}"
do
    URL="$BASE_URL/$file/$SPRING_VERSION/$file-$SPRING_VERSION.jar"
    echo "Téléchargement : $file"
    wget -q $URL
done

echo "Téléchargement terminé ✅"