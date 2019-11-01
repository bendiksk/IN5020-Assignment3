# IN5020-Assignment3
Simulation of a P2P protocol using the PeerSim simulator

---

## Instructions
**How to build in intelliJ**

If the IN5020-Assignment3.jar is not up to date, a new .jar must be built:

- Go to File > Project Structure > Project Settings > Artifacts
- Click "+" > JAR > From modules with dependencies...
    - In "Module:" select IN5020-Assignment3
    - in "Main Class:" select Simulator (peersim)
- Click OK
- Go to Build > Build Artifacts... > Build

**How to run**

`java -cp path/to/IN5020-Assignment3.jar peersim.Simulator path/to/peersimScript.txt`

Examples:

If the .jar build is up to date

`java -cp IN5020-Assignment3.jar peersim.Simulator scripts/RandomExample.txt`

From a fresh build (intelliJ)

`java -cp out/artifacts/IN5020_Assignment3_jar/IN5020-Assignment3.jar peersim.Simulator scripts/RandomExample.txt`
