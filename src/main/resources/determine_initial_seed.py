from argparse import ArgumentParser
from datetime import datetime, timedelta

RIGHT_BITS_RANGE_Y_ROT_SEED = 16_777_216


def determine_initial_seed(possibleNano, yRotSeed, uptimeTicks, test=False):
    numSeedsToCheck = 10_000_000_000
    if test:
        possibleNano = 1147024559000000
        yRotSeed = 84968831188992
        uptimeTicks = 390
        numSeedsToCheck = 50
    seedNumSeedsAwayFromUpNano = possibleNano - numSeedsToCheck
    simulateRNGFor = timedelta(milliseconds=(uptimeTicks * 50 * 20000))
    iSeed = possibleNano
    while possibleNano >= seedNumSeedsAwayFromUpNano:
        stopSimulatingRNGAt = datetime.now() + simulateRNGFor
        iSeed = iSeed * 25214903917 + 11 & 281474976710655
        for i in range(0, RIGHT_BITS_RANGE_Y_ROT_SEED):
            wholeYRotSeed = yRotSeed + i
            if wholeYRotSeed == iSeed:
                print(f"Seed sequence starting at initialSeed={iSeed} generates measuredSeed={wholeYRotSeed}.")
                exit(0)
        if datetime.now() > stopSimulatingRNGAt:
            possibleNano -= 1
            iSeed = possibleNano
            stopSimulatingRNGAt = datetime.now() + simulateRNGFor


if __name__ == '__main__':
    argParse = ArgumentParser()
    argParse.add_argument("upNano", type=int)
    argParse.add_argument("yRotSeed", type=int)
    argParse.add_argument("uptimeTicks", type=int)
    args = argParse.parse_args()
    determine_initial_seed(args.upNano, args.yRotSeed, args.uptimeTicks, test=True)
