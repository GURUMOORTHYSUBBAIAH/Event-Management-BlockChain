// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

contract EventTicketNFT is ERC721, Ownable, ReentrancyGuard {
    struct TicketData {
        uint256 eventId;
        bool attendanceMarked;
        bytes32 certificateHash;
    }

    uint256 private _nextTokenId;
    mapping(uint256 => TicketData) private _ticketData;

    event TicketMinted(uint256 indexed tokenId, address indexed to, uint256 eventId);
    event AttendanceMarked(uint256 indexed tokenId);
    event CertificateIssued(uint256 indexed tokenId, bytes32 certificateHash);

    constructor() ERC721("EventTicket", "ETKT") Ownable(msg.sender) {}

    function mintTicket(address to, uint256 eventId, string calldata metadataUri)
        external
        onlyOwner
        nonReentrant
        returns (uint256)
    {
        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);
        _ticketData[tokenId] = TicketData({
            eventId: eventId,
            attendanceMarked: false,
            certificateHash: bytes32(0)
        });
        emit TicketMinted(tokenId, to, eventId);
        return tokenId;
    }

    function markAttendance(uint256 tokenId) external onlyOwner nonReentrant {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        require(!_ticketData[tokenId].attendanceMarked, "Already marked");
        _ticketData[tokenId].attendanceMarked = true;
        emit AttendanceMarked(tokenId);
    }

    function issueCertificateHash(uint256 tokenId, bytes32 certHash)
        external
        onlyOwner
        nonReentrant
    {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        require(_ticketData[tokenId].certificateHash == bytes32(0), "Certificate already issued");
        _ticketData[tokenId].certificateHash = certHash;
        emit CertificateIssued(tokenId, certHash);
    }

    function getTicketData(uint256 tokenId)
        external
        view
        returns (uint256 eventId, bool attendanceMarked, bytes32 certificateHash)
    {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        TicketData memory data = _ticketData[tokenId];
        return (data.eventId, data.attendanceMarked, data.certificateHash);
    }

    function getCertificateHash(uint256 tokenId) external view returns (bytes32) {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        return _ticketData[tokenId].certificateHash;
    }
}
