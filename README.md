Project flow:

- Main.java
  - Setup settings
  - Create VRPInstance
  - VRPInstance.solve
    - CPInstance.getFeasible - Finds a starting bin packing
    - LSInstance.solve - Perform local search starting at feasible model
      - VRPLocalSearchRandom.search (AbstractLocalSearch.search)
      - Until time limit reached
        - VRPState.getRandom - Get random neighbor
          - Remove random number of customers from bin
          - CPInstance.solveBin - Refill bins
        - VRPState.getValue
          - For each bin
            - TSPLocalSearch.solve - Perform nearest neighbor search
      - Return best solution
  - Print result
