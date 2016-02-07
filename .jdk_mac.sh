if [ "$TRAVIS_OS_NAME" = "osx" ]; then

brew tap caskroom/cask
brew tap caskroom/versions

switch_to_oraclejdk7()
{
brew cask install java7
}

switch_to_oraclejdk8()
{
brew cask install java
}

switch_jdk()
{
    case "${1:-default}" in
        oraclejdk7)
            switch_to_oraclejdk7
            ;;
        oraclejdk8)
            switch_to_oraclejdk8
            ;;
    esac
}


jdk_switcher()
{
    typeset COMMAND JDK
    COMMAND="$1"
    JDK="$2"

    case "$COMMAND" in
        use)
            switch_jdk "$JDK"
            ;;
    esac

    return $?
}


fi