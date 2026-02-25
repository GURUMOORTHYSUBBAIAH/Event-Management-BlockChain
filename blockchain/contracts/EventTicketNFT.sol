// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

contract EventTicketNFT is ERC721, Ownable, AccessControl, ReentrancyGuard {
    struct TicketData {
        uint256 eventId;
        bool attendanceMarked;
        bytes32 certificateHash;
        uint256 mintedAt;
    }

    bytes32 public constant MINTER_ROLE = keccak256("MINTER_ROLE");
    bytes32 public constant ATTENDANCE_ROLE = keccak256("ATTENDANCE_ROLE");
    
    uint256 private _nextTokenId;
    mapping(uint256 => TicketData) private _ticketData;
    mapping(uint256 => bool) private _validEvents;
    
    event TicketMinted(uint256 indexed tokenId, address indexed to, uint256 eventId);
    event AttendanceMarked(uint256 indexed tokenId);
    event CertificateIssued(uint256 indexed tokenId, bytes32 certificateHash);
    event EventAdded(uint256 indexed eventId);
    event EventRemoved(uint256 indexed eventId);

    constructor() ERC721("EventTicket", "ETKT") Ownable(msg.sender) {
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(MINTER_ROLE, msg.sender);
        _grantRole(ATTENDANCE_ROLE, msg.sender);
    }

    function addEvent(uint256 eventId) external onlyRole(DEFAULT_ADMIN_ROLE) {
        require(!_validEvents[eventId], "Event already exists");
        _validEvents[eventId] = true;
        emit EventAdded(eventId);
    }

    function removeEvent(uint256 eventId) external onlyRole(DEFAULT_ADMIN_ROLE) {
        require(_validEvents[eventId], "Event does not exist");
        _validEvents[eventId] = false;
        emit EventRemoved(eventId);
    }

    function mintTicket(address to, uint256 eventId, string calldata metadataUri)
        external
        onlyRole(MINTER_ROLE)
        nonReentrant
        returns (uint256)
    {
        require(_validEvents[eventId], "Invalid event");
        require(to != address(0), "Invalid recipient");
        
        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);
        _ticketData[tokenId] = TicketData({
            eventId: eventId,
            attendanceMarked: false,
            certificateHash: bytes32(0),
            mintedAt: block.timestamp
        });
        emit TicketMinted(tokenId, to, eventId);
        return tokenId;
    }

    function markAttendance(uint256 tokenId) external onlyRole(ATTENDANCE_ROLE) nonReentrant {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        require(!_ticketData[tokenId].attendanceMarked, "Already marked");
        _ticketData[tokenId].attendanceMarked = true;
        emit AttendanceMarked(tokenId);
    }

    function issueCertificateHash(uint256 tokenId, bytes32 certHash)
        external
        onlyRole(ATTENDANCE_ROLE)
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
        returns (uint256 eventId, bool attendanceMarked, bytes32 certificateHash, uint256 mintedAt)
    {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        TicketData memory data = _ticketData[tokenId];
        return (data.eventId, data.attendanceMarked, data.certificateHash, data.mintedAt);
    }

    function getCertificateHash(uint256 tokenId) external view returns (bytes32) {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        return _ticketData[tokenId].certificateHash;
    }

    function isEventValid(uint256 eventId) external view returns (bool) {
        return _validEvents[eventId];
    }

    function totalSupply() external view returns (uint256) {
        return _nextTokenId;
    }

    function supportsInterface(bytes4 interfaceId) public view virtual override(ERC721, AccessControl) returns (bool) {
        return super.supportsInterface(interfaceId);
    }
}
