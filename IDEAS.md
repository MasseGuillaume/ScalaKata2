getting started

stackoverflow
  to scalakata
  from scalakata

build info available in frontend
  version & git hash
  scalacOptions
  libraryDependencies

load balance compiler with akka + typed actors

books
  fp in scala
  atomic scala

apache spark

scalajs output
  live edit games
  viz for spark

nix package

use nix package to provision docker

dynamic dependencies
  via apache aether or courrier
  scalacOptions

collaborative
  show a grid of collaborative coding

insight with types
  need to do c.typecheck and c.untypecheck because whitebox macros are untyped.
  the later (c.untypecheck) does not work really well.

compiler plugin vs annotation macro

annotation macro
  allow more than one annotation macro

codemirror 
  mixed mode for q"" and s""
  nice ui for options (dropdown & etc)
  line target
    ```css
    .CodeMirror-linenumber {
      cursor: pointer;
    }
    ```

    ```scala
    editor.on("gutterClick", (cm: Editor, n: js.Any) => {
      // n.asInstanceOf[Int]
      // clear previous removeLineClass
      // editor.addLineClass
      // set target
    });
    ```

continious release
  ```yaml
  env: 
    global: 
    - secure: |-
        IE7h19w4yvHmFdg8Z8qd4a+53hZGXt9n6UoIkzG2XN+HN3b7Th00Mf7Glllo
        h+SZyDnkKVrJC3prnzyMYCg9/gddTV94ffE+/IxulMbIHbTSiEmLPxHYpffY
        NyXdxHg8SLSN8I8vHbl1NFh/Qf9M/h8J1cFknDpiKFRoFg3VuSlUKODJqEnL
        v2hNpEdb22cME3XH6xzHVUTyb4lcEkejrQvfNhtGXFOgimwUVMLKfPrw4/LT
        letXwMSOMKdb7IP3hEKVs8nYET07lafIwyDljzMJU+wljYh+fXX5uBvhLgBv
        Hqdv77DS2KHEiBnN/IL8q2ipBTR6J9kLM5lK3DFdlM8CY86vk6XP5EnPcXcB
        j7wWzf5igcmzrwK/46pESklWUA+UKhq+CU3lcnuD2SU8S0rlcH9hyKiu8zFI
        e29R/ObvmoMO0gIDmV3bd/0HNyt5M05e0OgQtGhHUHhOwq9lIg5WO0bAiJZV
        /fa09jW6H8CAnJ0j3AwHop97SqPDYbOyvdpj4SQmwM0WIKF4gqfYQg6C0uGc
        RT7ly1//16IHiKiT+XL34rgTJrrVMoNDzs65Kw4A4TmtryFh/LO28e9AsaEn
        56zr0rebfxfQpJeTk1p1oAugKiGvbV5PeubRtG20Cz0ZKI+8m3WskIDNpSKv
        +4dEFJOm7q7nH/8ZKWL+F/0=
  before_script: bintray.sh
  ```

  ```scala
  bintrayCredentialsFile := file("credentials")
  ```

  ```bash
  echo "
  realm = Bintray API Realm
  host = api.bintray.com
  user = masseguillaume
  password = $BINTRAY_API_KEY" > credentials
  ```
 
scalameta
  for macro (try tree.toGTree)
  instrument everything
  for typeAt
  for completion (ask Eugene)