const hre = require("hardhat");

async function main() {
  const [deployer] = await hre.ethers.getSigners();
  console.log("Deploying with account:", deployer.address);

  const EventTicketNFT = await hre.ethers.getContractFactory("EventTicketNFT");
  const nft = await EventTicketNFT.deploy();
  await nft.waitForDeployment();
  const address = await nft.getAddress();
  console.log("EventTicketNFT deployed to:", address);

  const fs = require("fs");
  const artifact = await hre.artifacts.readArtifact("EventTicketNFT");
  fs.writeFileSync(
    "../backend/src/main/resources/abi/EventTicketNFT.json",
    JSON.stringify(artifact.abi, null, 2)
  );
  console.log("ABI written to backend resources");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
