from argparse import ArgumentParser
from datetime import datetime, timedelta


def generateSeedSequence(initialSeed):


    count = 0
    seedSequence = [initialSeed]
    nextSeed = nextS(initialSeed)
    while nextSeed != 0 and count < 1000000:
        seedSequence.append(nextSeed)
        nextSeed = nextS(nextSeed)
        count += 1
    return seedSequence
def nextS(seed):
    return seed * 25214903917 + 11 & 281474976710655


if __name__ == '__main__':
    argsParse = ArgumentParser()
    argsParse.add_argument("startingNano", type=int)
    argsParse.add_argument("detectionSeed", type=int)
    argsParse.add_argument("serverLevelUptimeNanos", type=int)
    args = argsParse.parse_args()

    possibleInitialSeed = args.startingNano
    seedToDetect = args.detectionSeed
    serverLevelUptime = args.serverLevelUptimeNanos
    currSeed = possibleInitialSeed
    stopLookingAt = possibleInitialSeed - 10_000_000_000
    i = 0
    generateFor = timedelta(microseconds=(serverLevelUptime / 1000))
    stopGeneratingAt = datetime.now() + generateFor
    while currSeed >= stopLookingAt:
        if datetime.now() >= stopGeneratingAt:
            stopGeneratingAt = datetime.now() + generateFor
            i += 1
            currSeed = possibleInitialSeed - i
        currSeed = nextS(currSeed)
        if currSeed == seedToDetect:
            print(f"Seed sequence starting at initialSeed={currSeed} generates measuredSeed={seedToDetect}.")
            exit(0)

    print("Unsuccessful brute force attempt. Seed to detect was not found.")
    exit(1)


