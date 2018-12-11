pragma solidity ^0.4.18;

contract Owners {

    //@dev These events occur when the owner change agenda is registered / confirmed / revoked / executed.
    event OwnerChangeSubmission(uint indexed ownerChangeId, address indexed owner, string message);
    event OwnerChangeConfirmation(address indexed owner, uint indexed changeId);
    event OwnerChangeRevocation(address indexed owner, uint indexed changeId);
    event OwnerAddition(address indexed owner);
    event OwnerRemoval(address indexed owner);

    //@dev These events occur when the requirement change agenda is registered / confirmed / revoked / executed.
    event RequirementChangeSubmission(uint indexed requiredChangeId, uint require);
    event RequirementChangeConfirmation(address indexed owner, uint indexed changeId);
    event RequirementChangeRevocation(address indexed owner, uint indexed changeId);
    event RequirementChangeExecution(uint changeId);

    uint16 constant public MAX_OWNER_COUNT = 50;


    mapping(uint24 => OwnerChange) public ownerChanges;
    mapping(uint24 => RequirementChange) public requirementChanges;

    mapping(uint24 => mapping (address => bool)) public ownerChangeConfirmations;
    mapping(uint24 => mapping (address => bool)) public requirementChangeConfirmations;

    mapping(address => bool) public isOwner;

    address[] public owners;
    uint16 public required;

    uint24 public requirementChangeCount;
    uint24 public ownerChangeCount;


    struct OwnerChange {
        address owner;
        bool isAdd;
        bool executed;
    }

    struct RequirementChange {
        uint16 requirement;
        bool executed;
    }




    /**
     * @dev The called address must be in the owner list.
     */
    modifier ownerExists(address _owner) {
        require(isOwner[_owner]);
        _;
    }

    /**
     * @dev The called address must not be in the owner list.
     */
    modifier ownerDoesNotExist(address owner) {
        require(!isOwner[owner]);
        _;
    }

    /**
     * @dev The address should not be empty.
     */
    modifier notNull(address _address) {
        require(_address != 0x0);
        _;
    }

    /**
     * @dev The minimum quorum should be the correct value.
     */
    modifier validRequirement (uint256 _ownerCount, uint16 _required) {
        require(_ownerCount <= MAX_OWNER_COUNT
        && _required <= _ownerCount
        && _required != 0
        && _ownerCount != 0);
        _;
    }

    modifier emptyOwner () {
        require(owners.length == 0 && required == 0);
        _;
    }


    /**
     * @dev "_owner" should confirm the "_changeId" agenda.
     */
    modifier confirmedOwnerChange(uint24 _changeId, address _owner) {
        require(ownerChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev "_owner" should not have confirmed the "_changeId" agenda.
     */
    modifier notConfirmedOwnerChange(uint24 _changeId, address _owner) {
        require(!ownerChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev The "_changeId" item should not have been executed.
     */
    modifier notExecutedOwnerChange(uint24 _changeId) {
        require(!ownerChanges[_changeId].executed);
        _;
    }



    /**
     * @dev "_owner" should confirm the "_changeId" agenda.
     */
    modifier confirmedRequirement(uint24 _changeId, address _owner) {
        require(requirementChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev "_owner" should not have confirmed the "_changeId" agenda.
     */
    modifier notConfirmedRequirement(uint24 _changeId, address _owner) {
        require(!requirementChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev The "_changeId" item should not have been executed.
     */
    modifier notExecutedRequirement(uint24 _changeId) {
        require(!requirementChanges[_changeId].executed);
        _;
    }








    //------------------------------------------------------------
    // MultiSig : Owner Add/Remove process
    //------------------------------------------------------------
    /**
     * @dev Register an agenda to add "_owner"
     *      The owner who registers the item will automatically approve the item.
     *      If the minimum quorum(required) is 1, the item is executed at the same time as the item is registered.
     * @return ownerChangeId ID of the agenda
     */
    function registerOwnerAdd(address _owner)
    public
    notNull(_owner)
    ownerExists(msg.sender)
    ownerDoesNotExist(_owner)
    returns (uint ownerChangeId)
    {
        return registerChangeOwner(_owner, true);
    }

    /**
     * @dev Register an agenda that removes "_owner" from the owner list.
     */
    function registerOwnerRemove(address _owner)
    public
    notNull(_owner)
    ownerExists(msg.sender)
    ownerExists(_owner)
    returns (uint ownerChangeId)
    {
        return registerChangeOwner(_owner, false);
    }


    function registerChangeOwner(address _owner, bool _isAdd)
    internal
    returns (uint24 ownerChangeId)
    {
        ownerChangeId = ownerChangeCount;

        ownerChanges[ownerChangeId] = OwnerChange({
            owner : _owner,
            isAdd : _isAdd,
            executed : false
            });

        ownerChangeCount += 1;
        if(_isAdd) {
            emit OwnerChangeSubmission(ownerChangeId, _owner, "Add");
        } else {
            emit OwnerChangeSubmission(ownerChangeId, _owner, "Remove");
        }

        confirmOwnerChange(ownerChangeId);
    }


    function confirmOwnerChange(uint24 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedOwnerChange(_changeId)
    notConfirmedOwnerChange(_changeId, msg.sender)
    {
        ownerChangeConfirmations[_changeId][msg.sender] = true;
        emit OwnerChangeConfirmation(msg.sender, _changeId);

        executeOwnerChange(_changeId);
    }

    function revokeOwnerChangeConfirmation(uint24 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedOwnerChange(_changeId)
    confirmedOwnerChange(_changeId, msg.sender)
    {
        ownerChangeConfirmations[_changeId][msg.sender] = false;
        emit OwnerChangeRevocation(msg.sender, _changeId);
    }

    function executeOwnerChange(uint24 _changeId)
    internal
    ownerExists(msg.sender)
    notExecutedOwnerChange(_changeId)
    confirmedOwnerChange(_changeId, msg.sender)
    {
        if(isOwnerChangeConfirmed(_changeId)) {
            OwnerChange storage ownerChange = ownerChanges[_changeId];

            if(ownerChange.isAdd) {
                addOwner(ownerChange.owner);
            }
            else {
                removeOwner(ownerChange.owner);
            }

            ownerChange.executed = true;
        }
    }

    function isOwnerChangeConfirmed(uint24 _changeId)
    internal
    constant
    returns (bool)
    {
        uint count = 0;
        for(uint i = 0; i < owners.length; i++) {
            if(ownerChangeConfirmations[_changeId][owners[i]])
                count += 1;
            if(count == required)
                return true;
        }
    }


    function getCountOfOwnerChangeConfirms(uint24 _changeId)
    public
    constant
    returns (uint)
    {
        uint count = 0;
        for(uint i = 0; i < owners.length; i++) {
            if(ownerChangeConfirmations[_changeId][owners[i]])
                count += 1;
        }
    }


    function addOwner(address _owner)
    internal
    ownerDoesNotExist(_owner)
    {
        isOwner[_owner] = true;
        owners.push(_owner);

        emit OwnerAddition(_owner);
    }


    function removeOwner(address _owner)
    internal
    ownerExists(_owner)
    {
        isOwner[_owner] = false;

        for(uint i = 0 ; i < owners.length; i++) {
            if(owners[i] == _owner) {
                owners[i] = owners[owners.length - 1];
                break;
            }
        }

        owners.length -= 1;

        emit OwnerRemoval(_owner);
    }







    //------------------------------------------------------------
    // MultiSig : Requirement change process
    //------------------------------------------------------------
    function registerRequirementChange(uint16 _requirement)
    public
    ownerExists(msg.sender)
    validRequirement(owners.length, _requirement)
    returns (uint24 requirementChangeId)
    {
        requirementChangeId = requirementChangeCount;
        requirementChanges[requirementChangeId] = RequirementChange({
            requirement : _requirement,
            executed : false
            });

        requirementChangeCount += 1;
        emit RequirementChangeSubmission(requirementChangeId, _requirement);

        confirmRequirementChange(requirementChangeId);
    }


    function confirmRequirementChange(uint24 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedRequirement(_changeId)
    notConfirmedRequirement(_changeId, msg.sender)
    {
        requirementChangeConfirmations[_changeId][msg.sender] = true;
        emit RequirementChangeConfirmation(msg.sender, _changeId);

        executeRequirementChange(_changeId);
    }

    function revokeRequirementChangeConfirmation(uint24 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedRequirement(_changeId)
    confirmedRequirement(_changeId, msg.sender)
    {
        requirementChangeConfirmations[_changeId][msg.sender] = false;
        emit RequirementChangeRevocation(msg.sender, _changeId);
    }


    function executeRequirementChange(uint24 _changeId)
    internal
    ownerExists(msg.sender)
    notExecutedRequirement(_changeId)
    confirmedRequirement(_changeId, msg.sender)
    {
        if(isRequirementChangeConfirmed(_changeId)) {
            RequirementChange storage requirementChange = requirementChanges[_changeId];

            required = requirementChange.requirement;
            requirementChange.executed = true;

            emit RequirementChangeExecution(_changeId);
        }
    }


    function isRequirementChangeConfirmed(uint24 _changeId)
    internal
    constant
    returns (bool)
    {
        uint count = 0;
        for(uint24 i = 0; i < owners.length; i++) {
            if(requirementChangeConfirmations[_changeId][owners[i]])
                count += 1;
            if(count == required)
                return true;
        }
    }


    function getCountOfRequirementChangeConfirms(uint24 _changeId)
    public
    constant
    returns (uint count)
    {
        count = 0;
        for(uint24 i = 0; i < owners.length; i++) {
            if(requirementChangeConfirmations[_changeId][owners[i]])
                count += 1;
        }
    }
}



/**
 *
 */
contract Domain is Owners {
    using strings for *;


    event ApprovalDelegatorChange (address newDelegator);

    event WithdrawalSubmission (uint indexed id, uint256 aApis);
    event WithdrawalConfirmation (uint indexed id);
    event WithdrawalRevocation (uint indexed id);
    event WithdrawalExecution(uint indexed id, uint256 aApis);


    // @dev Name after "@"
    string public domainName;

    // @dev Maximum length of the name to the left of "@"
    uint constant public maxNameLength = 32;


    // @dev This address can handle approval by itself. It is assigned through the vote of several owners.
    address public approvalDelegator;

    address addressMaksingAddress = 0x1000000000000000000000000000000000037449;

    // Multisig
    mapping(uint => Withdrawal) public withdrawals;

    mapping(uint => mapping(address => bool)) public withdrawalConfirms;

    uint public withdrawalCount;


    struct Withdrawal {
        address receiver;
        uint256 aApis;
        bool executed;
    }




    modifier validDomainNameCharacter(string name) {
        require(name.toSlice().find("@".toSlice()).len() == 0);
        _;
    }

    modifier validNameLength(string name) {
        require(bytes(name).length <= maxNameLength);
        _;
    }

    modifier validWithdrawalAmount(uint256 amount) {
        require(amount > 0 && amount <= address(this).balance);
        _;
    }

    modifier withdrawalExist(uint _id) {
        require(withdrawals[_id].aApis > 0);
        _;
    }

    modifier notConfirmedWithdrawal (uint _id, address _owner) {
        require(withdrawalConfirms[_id][_owner] == false);
        _;
    }

    modifier confirmedWithdrawal (uint _id, address _owner) {
        require(withdrawalConfirms[_id][_owner] == true);
        _;
    }

    modifier notExecutedWithdrawal (uint _id) {
        require(withdrawals[_id].executed == false);
        _;
    }





    constructor (
        string      _domainName,
        address[]   _domainOwners,
        uint16        _required
    )
    public
    validDomainNameCharacter(_domainName)
    validRequirement(_domainOwners.length, _required)
    {
        domainName = _domainName;
        owners = _domainOwners;
        required = _required;

        for (uint i=0; i < _domainOwners.length; i++) {
            require(!isOwner[_domainOwners[i]] && _domainOwners[i] != 0);
            isOwner[_domainOwners[i]] = true;
        }
    }


    /**
     * @dev Domain 컨트렉트는 AddressMasking 컨트렉트에서 보내준 수수료만을 받을 수 있다.
     *      Domain contract can only receive commission from the AddressMasking contract.     *
     */
    function ()
    public
    payable
    {
        require(msg.sender == addressMaksingAddress);
    }


    /**
     * @dev 이 컨트렉트의 도메인 네임을 확인한다.
     *      Check the domain name of this contract.
     * @return string domainName : domain name of this contract
     */
    function getDomainName()
    public
    view
    returns (string)
    {
        return domainName;
    }


    /**
     * @dev 매개변수로 전달된 주소가 마스크 등록을 승인하는 권한을 가지고 있는지 확인한다.
     *      Ensure that the address passed as a parameter has the authority to grant mask registration.
     * @param _owner The address you want to check for permission
     * @return bool True, if _owner has permission
     */
    function isApprover (address _owner)
    public
    view
    returns (bool)
    {
        return (isOwner[_owner] || approvalDelegator == _owner);
    }





    function changeApprovalDelegator(address _newDelegator)
    public
    ownerExists(msg.sender) {
        approvalDelegator = _newDelegator;
        emit ApprovalDelegatorChange (_newDelegator);
    }







    function registerWithdrawal(address _receiver, uint256 _aApis)
    public
    ownerExists(msg.sender)
    validWithdrawalAmount(_aApis)
    returns (uint id)
    {
        id = withdrawalCount;
        withdrawals[id] = Withdrawal({
            aApis : _aApis,
            receiver : _receiver,
            executed : false
            });

        withdrawalCount += 1;

        emit WithdrawalSubmission(id, _aApis);

        confirmWithdrawal(id);
    }

    function confirmWithdrawal(uint _id)
    public
    ownerExists(msg.sender)
    withdrawalExist(_id)
    notExecutedWithdrawal(_id)
    notConfirmedWithdrawal(_id, msg.sender)
    {
        withdrawalConfirms[_id][msg.sender] = true;

        emit WithdrawalConfirmation(_id);

        executeWithdrawal(_id);
    }

    function revokeWithdrawalConfirmation (uint _id)
    public
    ownerExists(msg.sender)
    notExecutedWithdrawal(_id)
    confirmedWithdrawal(_id, msg.sender)
    {
        withdrawalConfirms[_id][msg.sender] = false;

        emit WithdrawalRevocation(_id);
    }

    function executeWithdrawal (uint _id)
    internal
    ownerExists(msg.sender)
    notExecutedWithdrawal(_id)
    confirmedWithdrawal(_id, msg.sender)
    validWithdrawalAmount(withdrawals[_id].aApis)
    {
        if(isWithdrawalConfirmed(_id)) {
            Withdrawal storage withdrawal = withdrawals[_id];
            withdrawal.executed = true;
            emit WithdrawalExecution(_id, withdrawal.aApis);

            address receiver = withdrawal.receiver;
            receiver.transfer(withdrawal.aApis);
        }
    }

    function isWithdrawalConfirmed (uint _id)
    internal
    constant
    returns (bool)
    {
        uint count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (withdrawalConfirms[_id][owners[i]])
                count += 1;
            if (count == required)
                return true;
        }
    }

    function getCountOfWithdrawalConfirms (uint _id)
    public
    constant
    returns (uint count)
    {
        count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (withdrawalConfirms[_id][owners[i]])
                count += 1;
        }
    }
}













contract PublicDomain {

    // @dev Name after "@"
    string domainName;

    modifier emptyDomainName() {
        bytes memory tempNameBytes = bytes(domainName);
        require(tempNameBytes.length == 0);
        _;
    }

    function ()
    public
    payable
    {
        revert();
    }

    function init(string _name)
    public
    emptyDomainName()
    {
        domainName = _name;
    }



    function getDomainName()
    public
    view
    returns (string)
    {
        return domainName;
    }
}














/*
 * @title String & slice utility library for Solidity contracts.
 * @author Nick Johnson <arachnid@notdot.net>
 *
 * @dev Functionality in this library is largely implemented using an
 *      abstraction called a 'slice'. A slice represents a part of a string -
 *      anything from the entire string to a single character, or even no
 *      characters at all (a 0-length slice). Since a slice only has to specify
 *      an offset and a length, copying and manipulating slices is a lot less
 *      expensive than copying and manipulating the strings they reference.
 *
 *      To further reduce gas costs, most functions on slice that need to return
 *      a slice modify the original one instead of allocating a new one; for
 *      instance, `s.split(".")` will return the text up to the first '.',
 *      modifying s to only contain the remainder of the string after the '.'.
 *      In situations where you do not want to modify the original slice, you
 *      can make a copy first with `.copy()`, for example:
 *      `s.copy().split(".")`. Try and avoid using this idiom in loops; since
 *      Solidity has no memory management, it will result in allocating many
 *      short-lived slices that are later discarded.
 *
 *      Functions that return two slices come in two versions: a non-allocating
 *      version that takes the second slice as an argument, modifying it in
 *      place, and an allocating version that allocates and returns the second
 *      slice; see `nextRune` for example.
 *
 *      Functions that have to copy string data will return strings rather than
 *      slices; these can be cast back to slices for further processing if
 *      required.
 *
 *      For convenience, some functions are provided with non-modifying
 *      variants that create a new slice and return both; for instance,
 *      `s.splitNew('.')` leaves s unmodified, and returns two values
 *      corresponding to the left and right parts of the string.
 */

library strings {
    struct slice {
        uint _len;
        uint _ptr;
    }

    function memcpy(uint dest, uint src, uint len) private pure {
        // Copy word-length chunks while possible
        for(; len >= 32; len -= 32) {
            assembly {
                mstore(dest, mload(src))
            }
            dest += 32;
            src += 32;
        }

        // Copy remaining bytes
        uint mask = 256 ** (32 - len) - 1;
        assembly {
            let srcpart := and(mload(src), not(mask))
            let destpart := and(mload(dest), mask)
            mstore(dest, or(destpart, srcpart))
        }
    }

    /*
     * @dev Returns a slice containing the entire string.
     * @param self The string to make a slice from.
     * @return A newly allocated slice containing the entire string.
     */
    function toSlice(string self) internal pure returns (slice) {
        uint ptr;
        assembly {
            ptr := add(self, 0x20)
        }
        return slice(bytes(self).length, ptr);
    }

    /*
     * @dev Returns the length of a null-terminated bytes32 string.
     * @param self The value to find the length of.
     * @return The length of the string, from 0 to 32.
     */
    function len(bytes32 self) internal pure returns (uint) {
        uint ret;
        if (self == 0)
            return 0;
        if (self & 0xffffffffffffffffffffffffffffffff == 0) {
            ret += 16;
            self = bytes32(uint(self) / 0x100000000000000000000000000000000);
        }
        if (self & 0xffffffffffffffff == 0) {
            ret += 8;
            self = bytes32(uint(self) / 0x10000000000000000);
        }
        if (self & 0xffffffff == 0) {
            ret += 4;
            self = bytes32(uint(self) / 0x100000000);
        }
        if (self & 0xffff == 0) {
            ret += 2;
            self = bytes32(uint(self) / 0x10000);
        }
        if (self & 0xff == 0) {
            ret += 1;
        }
        return 32 - ret;
    }

    /*
     * @dev Returns a slice containing the entire bytes32, interpreted as a
     *      null-termintaed utf-8 string.
     * @param self The bytes32 value to convert to a slice.
     * @return A new slice containing the value of the input argument up to the
     *         first null.
     */
    function toSliceB32(bytes32 self) internal pure returns (slice ret) {
        // Allocate space for `self` in memory, copy it there, and point ret at it
        assembly {
            let ptr := mload(0x40)
            mstore(0x40, add(ptr, 0x20))
            mstore(ptr, self)
            mstore(add(ret, 0x20), ptr)
        }
        ret._len = len(self);
    }

    /*
     * @dev Returns a new slice containing the same data as the current slice.
     * @param self The slice to copy.
     * @return A new slice containing the same data as `self`.
     */
    function copy(slice self) internal pure returns (slice) {
        return slice(self._len, self._ptr);
    }

    /*
     * @dev Copies a slice to a new string.
     * @param self The slice to copy.
     * @return A newly allocated string containing the slice's text.
     */
    function toString(slice self) internal pure returns (string) {
        string memory ret = new string(self._len);
        uint retptr;
        assembly { retptr := add(ret, 32) }

        memcpy(retptr, self._ptr, self._len);
        return ret;
    }

    /*
     * @dev Returns the length in runes of the slice. Note that this operation
     *      takes time proportional to the length of the slice; avoid using it
     *      in loops, and call `slice.empty()` if you only need to know whether
     *      the slice is empty or not.
     * @param self The slice to operate on.
     * @return The length of the slice in runes.
     */
    function len(slice self) internal pure returns (uint l) {
        // Starting at ptr-31 means the LSB will be the byte we care about
        uint ptr = self._ptr - 31;
        uint end = ptr + self._len;
        for (l = 0; ptr < end; l++) {
            uint8 b;
            assembly { b := and(mload(ptr), 0xFF) }
            if (b < 0x80) {
                ptr += 1;
            } else if(b < 0xE0) {
                ptr += 2;
            } else if(b < 0xF0) {
                ptr += 3;
            } else if(b < 0xF8) {
                ptr += 4;
            } else if(b < 0xFC) {
                ptr += 5;
            } else {
                ptr += 6;
            }
        }
    }

    /*
     * @dev Returns true if the slice is empty (has a length of 0).
     * @param self The slice to operate on.
     * @return True if the slice is empty, False otherwise.
     */
    function empty(slice self) internal pure returns (bool) {
        return self._len == 0;
    }

    /*
     * @dev Returns a positive number if `other` comes lexicographically after
     *      `self`, a negative number if it comes before, or zero if the
     *      contents of the two slices are equal. Comparison is done per-rune,
     *      on unicode codepoints.
     * @param self The first slice to compare.
     * @param other The second slice to compare.
     * @return The result of the comparison.
     */
    function compare(slice self, slice other) internal pure returns (int) {
        uint shortest = self._len;
        if (other._len < self._len)
            shortest = other._len;

        uint selfptr = self._ptr;
        uint otherptr = other._ptr;
        for (uint idx = 0; idx < shortest; idx += 32) {
            uint a;
            uint b;
            assembly {
                a := mload(selfptr)
                b := mload(otherptr)
            }
            if (a != b) {
                // Mask out irrelevant bytes and check again
                uint256 mask = ~(2 ** (8 * (32 - shortest + idx)) - 1);
                uint256 diff = (a & mask) - (b & mask);
                if (diff != 0)
                    return int(diff);
            }
            selfptr += 32;
            otherptr += 32;
        }
        return int(self._len) - int(other._len);
    }

    /*
     * @dev Returns true if the two slices contain the same text.
     * @param self The first slice to compare.
     * @param self The second slice to compare.
     * @return True if the slices are equal, false otherwise.
     */
    function equals(slice self, slice other) internal pure returns (bool) {
        return compare(self, other) == 0;
    }

    /*
     * @dev Extracts the first rune in the slice into `rune`, advancing the
     *      slice to point to the next rune and returning `self`.
     * @param self The slice to operate on.
     * @param rune The slice that will contain the first rune.
     * @return `rune`.
     */
    function nextRune(slice self, slice rune) internal pure returns (slice) {
        rune._ptr = self._ptr;

        if (self._len == 0) {
            rune._len = 0;
            return rune;
        }

        uint l;
        uint b;
        // Load the first byte of the rune into the LSBs of b
        assembly { b := and(mload(sub(mload(add(self, 32)), 31)), 0xFF) }
        if (b < 0x80) {
            l = 1;
        } else if(b < 0xE0) {
            l = 2;
        } else if(b < 0xF0) {
            l = 3;
        } else {
            l = 4;
        }

        // Check for truncated codepoints
        if (l > self._len) {
            rune._len = self._len;
            self._ptr += self._len;
            self._len = 0;
            return rune;
        }

        self._ptr += l;
        self._len -= l;
        rune._len = l;
        return rune;
    }

    /*
     * @dev Returns the first rune in the slice, advancing the slice to point
     *      to the next rune.
     * @param self The slice to operate on.
     * @return A slice containing only the first rune from `self`.
     */
    function nextRune(slice self) internal pure returns (slice ret) {
        nextRune(self, ret);
    }

    /*
     * @dev Returns the number of the first codepoint in the slice.
     * @param self The slice to operate on.
     * @return The number of the first codepoint in the slice.
     */
    function ord(slice self) internal pure returns (uint ret) {
        if (self._len == 0) {
            return 0;
        }

        uint word;
        uint length;
        uint divisor = 2 ** 248;

        // Load the rune into the MSBs of b
        assembly { word:= mload(mload(add(self, 32))) }
        uint b = word / divisor;
        if (b < 0x80) {
            ret = b;
            length = 1;
        } else if(b < 0xE0) {
            ret = b & 0x1F;
            length = 2;
        } else if(b < 0xF0) {
            ret = b & 0x0F;
            length = 3;
        } else {
            ret = b & 0x07;
            length = 4;
        }

        // Check for truncated codepoints
        if (length > self._len) {
            return 0;
        }

        for (uint i = 1; i < length; i++) {
            divisor = divisor / 256;
            b = (word / divisor) & 0xFF;
            if (b & 0xC0 != 0x80) {
                // Invalid UTF-8 sequence
                return 0;
            }
            ret = (ret * 64) | (b & 0x3F);
        }

        return ret;
    }

    /*
     * @dev Returns the keccak-256 hash of the slice.
     * @param self The slice to hash.
     * @return The hash of the slice.
     */
    function keccak(slice self) internal pure returns (bytes32 ret) {
        assembly {
            ret := keccak256(mload(add(self, 32)), mload(self))
        }
    }

    /*
     * @dev Returns true if `self` starts with `needle`.
     * @param self The slice to operate on.
     * @param needle The slice to search for.
     * @return True if the slice starts with the provided text, false otherwise.
     */
    function startsWith(slice self, slice needle) internal pure returns (bool) {
        if (self._len < needle._len) {
            return false;
        }

        if (self._ptr == needle._ptr) {
            return true;
        }

        bool equal;
        assembly {
            let length := mload(needle)
            let selfptr := mload(add(self, 0x20))
            let needleptr := mload(add(needle, 0x20))
            equal := eq(keccak256(selfptr, length), keccak256(needleptr, length))
        }
        return equal;
    }

    /*
     * @dev If `self` starts with `needle`, `needle` is removed from the
     *      beginning of `self`. Otherwise, `self` is unmodified.
     * @param self The slice to operate on.
     * @param needle The slice to search for.
     * @return `self`
     */
    function beyond(slice self, slice needle) internal pure returns (slice) {
        if (self._len < needle._len) {
            return self;
        }

        bool equal = true;
        if (self._ptr != needle._ptr) {
            assembly {
                let length := mload(needle)
                let selfptr := mload(add(self, 0x20))
                let needleptr := mload(add(needle, 0x20))
                equal := eq(sha3(selfptr, length), sha3(needleptr, length))
            }
        }

        if (equal) {
            self._len -= needle._len;
            self._ptr += needle._len;
        }

        return self;
    }

    /*
     * @dev Returns true if the slice ends with `needle`.
     * @param self The slice to operate on.
     * @param needle The slice to search for.
     * @return True if the slice starts with the provided text, false otherwise.
     */
    function endsWith(slice self, slice needle) internal pure returns (bool) {
        if (self._len < needle._len) {
            return false;
        }

        uint selfptr = self._ptr + self._len - needle._len;

        if (selfptr == needle._ptr) {
            return true;
        }

        bool equal;
        assembly {
            let length := mload(needle)
            let needleptr := mload(add(needle, 0x20))
            equal := eq(keccak256(selfptr, length), keccak256(needleptr, length))
        }

        return equal;
    }

    /*
     * @dev If `self` ends with `needle`, `needle` is removed from the
     *      end of `self`. Otherwise, `self` is unmodified.
     * @param self The slice to operate on.
     * @param needle The slice to search for.
     * @return `self`
     */
    function until(slice self, slice needle) internal pure returns (slice) {
        if (self._len < needle._len) {
            return self;
        }

        uint selfptr = self._ptr + self._len - needle._len;
        bool equal = true;
        if (selfptr != needle._ptr) {
            assembly {
                let length := mload(needle)
                let needleptr := mload(add(needle, 0x20))
                equal := eq(keccak256(selfptr, length), keccak256(needleptr, length))
            }
        }

        if (equal) {
            self._len -= needle._len;
        }

        return self;
    }

    event log_bytemask(bytes32 mask);

    // Returns the memory address of the first byte of the first occurrence of
    // `needle` in `self`, or the first byte after `self` if not found.
    function findPtr(uint selflen, uint selfptr, uint needlelen, uint needleptr) private pure returns (uint) {
        uint ptr = selfptr;
        uint idx;

        if (needlelen <= selflen) {
            if (needlelen <= 32) {
                bytes32 mask = bytes32(~(2 ** (8 * (32 - needlelen)) - 1));

                bytes32 needledata;
                assembly { needledata := and(mload(needleptr), mask) }

                uint end = selfptr + selflen - needlelen;
                bytes32 ptrdata;
                assembly { ptrdata := and(mload(ptr), mask) }

                while (ptrdata != needledata) {
                    if (ptr >= end)
                        return selfptr + selflen;
                    ptr++;
                    assembly { ptrdata := and(mload(ptr), mask) }
                }
                return ptr;
            } else {
                // For long needles, use hashing
                bytes32 hash;
                assembly { hash := sha3(needleptr, needlelen) }

                for (idx = 0; idx <= selflen - needlelen; idx++) {
                    bytes32 testHash;
                    assembly { testHash := sha3(ptr, needlelen) }
                    if (hash == testHash)
                        return ptr;
                    ptr += 1;
                }
            }
        }
        return selfptr + selflen;
    }

    // Returns the memory address of the first byte after the last occurrence of
    // `needle` in `self`, or the address of `self` if not found.
    function rfindPtr(uint selflen, uint selfptr, uint needlelen, uint needleptr) private pure returns (uint) {
        uint ptr;

        if (needlelen <= selflen) {
            if (needlelen <= 32) {
                bytes32 mask = bytes32(~(2 ** (8 * (32 - needlelen)) - 1));

                bytes32 needledata;
                assembly { needledata := and(mload(needleptr), mask) }

                ptr = selfptr + selflen - needlelen;
                bytes32 ptrdata;
                assembly { ptrdata := and(mload(ptr), mask) }

                while (ptrdata != needledata) {
                    if (ptr <= selfptr)
                        return selfptr;
                    ptr--;
                    assembly { ptrdata := and(mload(ptr), mask) }
                }
                return ptr + needlelen;
            } else {
                // For long needles, use hashing
                bytes32 hash;
                assembly { hash := sha3(needleptr, needlelen) }
                ptr = selfptr + (selflen - needlelen);
                while (ptr >= selfptr) {
                    bytes32 testHash;
                    assembly { testHash := sha3(ptr, needlelen) }
                    if (hash == testHash)
                        return ptr + needlelen;
                    ptr -= 1;
                }
            }
        }
        return selfptr;
    }

    /*
     * @dev Modifies `self` to contain everything from the first occurrence of
     *      `needle` to the end of the slice. `self` is set to the empty slice
     *      if `needle` is not found.
     * @param self The slice to search and modify.
     * @param needle The text to search for.
     * @return `self`.
     */
    function find(slice self, slice needle) internal pure returns (slice) {
        uint ptr = findPtr(self._len, self._ptr, needle._len, needle._ptr);
        self._len -= ptr - self._ptr;
        self._ptr = ptr;
        return self;
    }

    /*
     * @dev Modifies `self` to contain the part of the string from the start of
     *      `self` to the end of the first occurrence of `needle`. If `needle`
     *      is not found, `self` is set to the empty slice.
     * @param self The slice to search and modify.
     * @param needle The text to search for.
     * @return `self`.
     */
    function rfind(slice self, slice needle) internal pure returns (slice) {
        uint ptr = rfindPtr(self._len, self._ptr, needle._len, needle._ptr);
        self._len = ptr - self._ptr;
        return self;
    }

    /*
     * @dev Splits the slice, setting `self` to everything after the first
     *      occurrence of `needle`, and `token` to everything before it. If
     *      `needle` does not occur in `self`, `self` is set to the empty slice,
     *      and `token` is set to the entirety of `self`.
     * @param self The slice to split.
     * @param needle The text to search for in `self`.
     * @param token An output parameter to which the first token is written.
     * @return `token`.
     */
    function split(slice self, slice needle, slice token) internal pure returns (slice) {
        uint ptr = findPtr(self._len, self._ptr, needle._len, needle._ptr);
        token._ptr = self._ptr;
        token._len = ptr - self._ptr;
        if (ptr == self._ptr + self._len) {
            // Not found
            self._len = 0;
        } else {
            self._len -= token._len + needle._len;
            self._ptr = ptr + needle._len;
        }
        return token;
    }

    /*
     * @dev Splits the slice, setting `self` to everything after the first
     *      occurrence of `needle`, and returning everything before it. If
     *      `needle` does not occur in `self`, `self` is set to the empty slice,
     *      and the entirety of `self` is returned.
     * @param self The slice to split.
     * @param needle The text to search for in `self`.
     * @return The part of `self` up to the first occurrence of `delim`.
     */
    function split(slice self, slice needle) internal pure returns (slice token) {
        split(self, needle, token);
    }

    /*
     * @dev Splits the slice, setting `self` to everything before the last
     *      occurrence of `needle`, and `token` to everything after it. If
     *      `needle` does not occur in `self`, `self` is set to the empty slice,
     *      and `token` is set to the entirety of `self`.
     * @param self The slice to split.
     * @param needle The text to search for in `self`.
     * @param token An output parameter to which the first token is written.
     * @return `token`.
     */
    function rsplit(slice self, slice needle, slice token) internal pure returns (slice) {
        uint ptr = rfindPtr(self._len, self._ptr, needle._len, needle._ptr);
        token._ptr = ptr;
        token._len = self._len - (ptr - self._ptr);
        if (ptr == self._ptr) {
            // Not found
            self._len = 0;
        } else {
            self._len -= token._len + needle._len;
        }
        return token;
    }

    /*
     * @dev Splits the slice, setting `self` to everything before the last
     *      occurrence of `needle`, and returning everything after it. If
     *      `needle` does not occur in `self`, `self` is set to the empty slice,
     *      and the entirety of `self` is returned.
     * @param self The slice to split.
     * @param needle The text to search for in `self`.
     * @return The part of `self` after the last occurrence of `delim`.
     */
    function rsplit(slice self, slice needle) internal pure returns (slice token) {
        rsplit(self, needle, token);
    }

    /*
     * @dev Counts the number of nonoverlapping occurrences of `needle` in `self`.
     * @param self The slice to search.
     * @param needle The text to search for in `self`.
     * @return The number of occurrences of `needle` found in `self`.
     */
    function count(slice self, slice needle) internal pure returns (uint cnt) {
        uint ptr = findPtr(self._len, self._ptr, needle._len, needle._ptr) + needle._len;
        while (ptr <= self._ptr + self._len) {
            cnt++;
            ptr = findPtr(self._len - (ptr - self._ptr), ptr, needle._len, needle._ptr) + needle._len;
        }
    }

    /*
     * @dev Returns True if `self` contains `needle`.
     * @param self The slice to search.
     * @param needle The text to search for in `self`.
     * @return True if `needle` is found in `self`, false otherwise.
     */
    function contains(slice self, slice needle) internal pure returns (bool) {
        return rfindPtr(self._len, self._ptr, needle._len, needle._ptr) != self._ptr;
    }

    /*
     * @dev Returns a newly allocated string containing the concatenation of
     *      `self` and `other`.
     * @param self The first slice to concatenate.
     * @param other The second slice to concatenate.
     * @return The concatenation of the two strings.
     */
    function concat(slice self, slice other) internal pure returns (string) {
        string memory ret = new string(self._len + other._len);
        uint retptr;
        assembly { retptr := add(ret, 32) }
        memcpy(retptr, self._ptr, self._len);
        memcpy(retptr + self._len, other._ptr, other._len);
        return ret;
    }

    /*
     * @dev Joins an array of slices, using `self` as a delimiter, returning a
     *      newly allocated string.
     * @param self The delimiter to use.
     * @param parts A list of slices to join.
     * @return A newly allocated string containing all the slices in `parts`,
     *         joined with `self`.
     */
    function join(slice self, slice[] parts) internal pure returns (string) {
        if (parts.length == 0)
            return "";

        uint length = self._len * (parts.length - 1);
        for(uint i = 0; i < parts.length; i++)
            length += parts[i]._len;

        string memory ret = new string(length);
        uint retptr;
        assembly { retptr := add(ret, 32) }

        for(i = 0; i < parts.length; i++) {
            memcpy(retptr, parts[i]._ptr, parts[i]._len);
            retptr += parts[i]._len;
            if (i < parts.length - 1) {
                memcpy(retptr, self._ptr, self._len);
                retptr += self._len;
            }
        }

        return ret;
    }
}



contract AddressMasking is Owners {
    using strings for *;



    event MaskAddition (address indexed face, string mask);
    event MaskHandOver (string mask, address oldAddress, address newAddress);

    event DomainRegistrationSubmission  (uint indexed domainRegistrationId, address indexed domainAddress, uint256 domainFee, uint256 foundationFee, string domainName, bool isOpened);
    event DomainRegistrationConfirmation(uint indexed domainRegistrationId);
    event DomainRegistrationRevocation  (uint indexed domainRegistrationId);
    event DomainRegistrationExecution   (uint indexed domainRegistrationId);

    event DefaultFeeChangeSubmission    (uint indexed defaultFeeChangeId, uint indexed aApis);
    event DefaultFeeChangeConfirmation  (uint indexed defaultFeeChangeId);
    event DefaultFeeChangeRevocation    (uint indexed defaultFeeChangeId);
    event DefaultFeeChangeExecution     (uint indexed defaultFeeChangeId);

    event DomainConfigChangeSubmission   (uint indexed domainConfigChangeId, uint indexed domainId, uint256 domainFee, uint256 foundationFee, bool isOpened);
    event DomainConfigChangeConfirmation (uint indexed domainConfigChangeId);
    event DomainConfigChangeRevocation   (uint indexed domainConfigChangeId);
    event DomainConfigChangeExecution    (uint indexed domainConfigChangeId);



    // @dev Maximum length of the name to the left of "@", length by RFC 2822
    uint constant public MAX_NAME_LENGTH = 64;

    // @dev If the fee is free, some attacker may generate a lot of transactions and attack the network.
    uint256 public defaultFee;

    // @dev Address of the Foundation to Manage Fees
    address foundationAccount;



    mapping(address => bytes32) public masks;
    mapping(address => string) public maskNames;
    mapping(bytes32 => address) public faces;

    address[] public domainContractAddresses;
    mapping(address => bool) public isDomainRegistered;
    mapping(address => DomainConfig) public domainConfigs;


    // MultiSig
    mapping(uint32 => DomainRegistration) public domainRegistrations;
    mapping(uint32 => DefaultFeeChange) public defaultFeeChanges;
    mapping(uint32 => DomainConfigChange) public domainConfigChanges;

    mapping(uint32 => mapping(address => bool)) public domainRegistrationConfirms;
    mapping(uint32 => mapping(address => bool)) public defaultFeeChangeConfirms;
    mapping(uint32 => mapping(address => bool)) public domainConfigChangeConfirms;

    uint32 public domainCount;
    uint32 public domainRegistrationCount;
    uint32 public defaultFeeChangeCount;
    uint32 public domainConfigChangeCount;



    struct DomainConfig {
        address domainAddress;
        uint256 domainFee;
        uint256 foundationFee;
        string domainName;
        bool needApproval;
        bool isOpened;
    }

    struct DomainRegistration {
        address domainAddress;
        uint256 domainFee;
        uint256 foundationFee;
        string domainName;
        bool isOpened;
        bool needApproval;
        bool executed;
    }

    struct DefaultFeeChange {
        bool registered;
        uint256 defaultFee;
        bool executed;
    }

    struct DomainConfigChange {
        uint256 domainFee;
        uint256 foundationFee;
        uint256 domainId;
        bool registered;
        bool isOpened;
        bool needApproval;
        bool executed;
    }






    modifier faceExist(address faceAddress) {
        require(masks[faceAddress] != 0x0);
        _;
    }

    modifier faceDoesNotExist(address faceAddress) {
        require(masks[faceAddress] == 0x0);
        _;
    }

    modifier maskDoesNotExist(address faceAddress, string name, uint32 domainId) {
        string memory domainName = domainConfigs[domainContractAddresses[domainId]].domainName;
        string memory addressMask = name.toSlice().concat("@".toSlice()).toSlice().concat(domainName.toSlice());

        bytes32 maskHash = keccak256(bytes(addressMask));
        require(faces[maskHash] == 0x0);
        _;
    }



    modifier validNameLength(string name) {
        require(bytes(name).length <= MAX_NAME_LENGTH);
        _;
    }

    modifier validMaskingFee(uint256 fee, uint32 domainId) {
        require(fee == domainConfigs[domainContractAddresses[domainId]].domainFee + domainConfigs[domainContractAddresses[domainId]].foundationFee + defaultFee);
        _;
    }

    modifier validChangingFee(uint256 fee) {
        require(fee == defaultFee);
        _;
    }

    /**
     * @dev Verify that the address entered is a domain contract.
     */
    modifier domainIdExist(uint32 domainId) {
        //require(isDomainRegistered[domain] == true);
        require(isDomainRegistered[domainContractAddresses[domainId]]);
        _;
    }

    modifier domainAddressExist(address domainAddress) {
        require(isDomainRegistered[domainAddress]);
        _;
    }

    modifier domainAddressNotExist(address domainAddress) {
        require(isDomainRegistered[domainAddress] == false);
        _;
    }

    modifier domainIsOpen(uint32 domainId) {
        require(domainConfigs[domainContractAddresses[domainId]].isOpened);
        _;
    }

    modifier domainAddressOpen(address domainAddress) {
        require(domainConfigs[domainAddress].isOpened);
        _;
    }






    modifier domainRegistrationExist(uint32 _id) {
        require(domainRegistrations[_id].domainAddress != 0x0);
        _;
    }

    modifier notConfirmedDomainRegistration (uint32 _id, address _owner) {
        require(domainRegistrationConfirms[_id][_owner] == false);
        _;
    }

    modifier confirmedDomainRegistration (uint32 _id, address _owner) {
        require(domainRegistrationConfirms[_id][_owner] == true);
        _;
    }

    modifier notExecutedDomainRegistration (uint32 _id) {
        require(domainRegistrations[_id].executed == false);
        _;
    }






    modifier defaultFeeChangeExist(uint32 _id) {
        require(defaultFeeChanges[_id].registered == true);
        _;
    }

    modifier notConfirmedDefaultFeeChange (uint32 _id, address _owner) {
        require(defaultFeeChangeConfirms[_id][_owner] == false);
        _;
    }

    modifier confirmedDefaultFeeChange (uint32 _id, address _owner) {
        require(defaultFeeChangeConfirms[_id][_owner] == true);
        _;
    }

    modifier notExecutedDefaultFeeChange (uint32 _id) {
        require(defaultFeeChanges[_id].executed == false);
        _;
    }





    modifier domainConfigChangeExist(uint32 _id) {
        require(domainConfigChanges[_id].registered == true);
        _;
    }

    modifier notConfirmedDomainConfigChange (uint32 _id, address _owner) {
        require(domainConfigChangeConfirms[_id][_owner] == false);
        _;
    }

    modifier confirmedDomainConfigChange (uint32 _id, address _owner) {
        require(domainConfigChangeConfirms[_id][_owner] == true);
        _;
    }

    modifier notExecutedDomainConfigChange (uint32 _id) {
        require(domainConfigChanges[_id].executed == false);
        _;
    }





    /**
     * @dev "@" is used to separate name and domain, so "@" is not allowed in names.
     */
    modifier validNameCharacter(string name) {
        require(name.toSlice().find("@".toSlice()).len() == 0 && name.toSlice().find(" ".toSlice()).len() == 0);
        _;
    }


    /**
     * @dev Contract constructor sets initial owners and required number of confirmations.
     * @param _owners List of initial owners.
     * @param _required Number of required confirmations.
     */
    function init (address[] _owners, uint16 _required)
    public
    validRequirement(_owners.length, _required)
    emptyOwner()
    {
        for (uint i = 0; i < _owners.length; i++) {
            require(!isOwner[_owners[i]] && _owners[i] != 0);
            isOwner[_owners[i]] = true;
        }

        owners = _owners;
        required = _required;

        defaultFee = 10*(10**18);

        foundationAccount = 0x1000000000000000000000000000000000037448;

        performDomainRegistration(0x1000000000000000000000000000000000070001, "me", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070002, "ico", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070003, "shop", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070004, "com", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070005, "org", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070006, "info", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070007, "biz", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070008, "net", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070009, "edu", false, true, 0, 0);
        performDomainRegistration(0x100000000000000000000000000000000007000a, "team", false, true, 0, 0);
        performDomainRegistration(0x100000000000000000000000000000000007000b, "pro", false, true, 0, 0);
        performDomainRegistration(0x100000000000000000000000000000000007000c, "xxx", false, true, 0, 0);
        performDomainRegistration(0x100000000000000000000000000000000007000d, "xyz", false, true, 0, 0);
        performDomainRegistration(0x100000000000000000000000000000000007000e, "cat", false, true, 0, 0);
        performDomainRegistration(0x100000000000000000000000000000000007000f, "dog", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070010, "exchange", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070011, "dapp", false, true, 0, 0);
        performDomainRegistration(0x1000000000000000000000000000000000070012, "firm", false, true, 0, 0);

        PublicDomain(0x1000000000000000000000000000000000070001).init("me");
        PublicDomain(0x1000000000000000000000000000000000070002).init("ico");
        PublicDomain(0x1000000000000000000000000000000000070003).init("shop");
        PublicDomain(0x1000000000000000000000000000000000070004).init("com");
        PublicDomain(0x1000000000000000000000000000000000070005).init("org");
        PublicDomain(0x1000000000000000000000000000000000070006).init("info");
        PublicDomain(0x1000000000000000000000000000000000070007).init("biz");
        PublicDomain(0x1000000000000000000000000000000000070008).init("net");
        PublicDomain(0x1000000000000000000000000000000000070009).init("edu");
        PublicDomain(0x100000000000000000000000000000000007000a).init("team");
        PublicDomain(0x100000000000000000000000000000000007000b).init("pro");
        PublicDomain(0x100000000000000000000000000000000007000c).init("xxx");
        PublicDomain(0x100000000000000000000000000000000007000d).init("xyz");
        PublicDomain(0x100000000000000000000000000000000007000e).init("cat");
        PublicDomain(0x100000000000000000000000000000000007000f).init("dog");
        PublicDomain(0x1000000000000000000000000000000000070010).init("exchange");
        PublicDomain(0x1000000000000000000000000000000000070011).init("dapp");
        PublicDomain(0x1000000000000000000000000000000000070012).init("firm");
    }


    function ()
    public
    payable
    {
        revert();
    }

    /**
     * @dev Returns the cost of registering an address.
     *      When you call the <i>registerMask</i> function, you need to send APIS as many as returned from <i>getRegistrationFee</i>.
     * @param _domainId The number of the domain you want to register
     */
    function getRegistrationFee(uint32 _domainId)
    external
    view
    returns (uint256 registrationFee)
    {
        registrationFee =
        defaultFee +
        domainConfigs[domainContractAddresses[_domainId]].domainFee +
        domainConfigs[domainContractAddresses[_domainId]].foundationFee;
    }


    /**
     * @dev aa
     */
    function registerMask (address _faceAddress, string _name, uint32 _domainId)
    public
    payable
    domainIdExist(_domainId)                // Pass if domain corresponding to _domainId exists
    domainIsOpen(_domainId)                 // Pass if domain is open
    validMaskingFee(msg.value, _domainId)   // Pass if fee is sufficient
    validNameLength(_name)                  // Pass if name of mask length is appropriate
    validNameCharacter(_name)               // Pass if the name does not contain the @ character
    faceDoesNotExist(_faceAddress)          // Pass if address(face) is not registered
    {
        // If an approval process is required, Only authorized addresses can register.
        if(domainConfigs[domainContractAddresses[_domainId]].needApproval == true) {
            Domain domainContract = Domain(domainContractAddresses[_domainId]);
            require(domainContract.isApprover(msg.sender));
        }

        applyMask(_faceAddress, _name, domainContractAddresses[_domainId]);
    }



    function applyMask (address _faceAddress, string _name, address _domainAddress)
    internal
    faceDoesNotExist(_faceAddress)
    {
        string memory domainName = domainConfigs[_domainAddress].domainName;
        string memory addressMask = _name.toSlice().concat("@".toSlice()).toSlice().concat(domainName.toSlice());

        emit MaskAddition(_faceAddress, addressMask);


        bytes32 maskHash = keccak256(bytes(addressMask));

        require(faces[maskHash] == 0x0);

        masks[_faceAddress] = maskHash;
        maskNames[_faceAddress] = addressMask;
        faces[maskHash] = _faceAddress;


        if(domainConfigs[_domainAddress].domainFee > 0) {
            _domainAddress.transfer(domainConfigs[_domainAddress].domainFee);
        }

        //Send a fee to the Foundation.
        foundationAccount.transfer(defaultFee + domainConfigs[_domainAddress].foundationFee);
    }

    function handOverMask (address _newAddress)
    public
    payable
    faceExist(msg.sender)
    validChangingFee(msg.value)
    {
        bytes32 maskHash = masks[msg.sender];
        string memory addressMask = maskNames[msg.sender];
        masks[_newAddress] = maskHash;
        maskNames[_newAddress] = addressMask;
        faces[maskHash] = _newAddress;

        // Remove
        masks[msg.sender] = 0x0;
        maskNames[msg.sender] = "";

        emit MaskHandOver (addressMask, msg.sender, _newAddress);

        foundationAccount.transfer(defaultFee);
    }



    function getFaceAddress (string _addressMask)
    public
    view
    returns (address faceAddress)
    {
        bytes32 maskHash = keccak256(bytes(_addressMask));
        faceAddress = faces[maskHash];
    }


    function getMaskHash (address _faceAddress)
    public
    view
    returns (bytes32 maskHash)
    {
        maskHash = masks[_faceAddress];
    }

    function getMaskName(address _faceAddress)
    public
    view
    returns (string maskName)
    {
        maskName = maskNames[_faceAddress];
    }


    function sizeOfDomain()
    public
    view
    returns (uint256 size) {
        size = domainContractAddresses.length;
    }


    function getDomainInfo(uint32 _domainId)
    public
    view
    returns (uint32 domainId, address domainAddress, string domainName, uint256 domainFee, uint256 foundationFee, bool needApproval, bool isOpened) {
        domainId = _domainId;
        domainAddress   = domainConfigs[domainContractAddresses[_domainId]].domainAddress;
        domainName      = domainConfigs[domainContractAddresses[_domainId]].domainName;
        domainFee       = domainConfigs[domainContractAddresses[_domainId]].domainFee;
        foundationFee   = domainConfigs[domainContractAddresses[_domainId]].foundationFee;
        needApproval    = domainConfigs[domainContractAddresses[_domainId]].needApproval;
        isOpened        = domainConfigs[domainContractAddresses[_domainId]].isOpened;
    }





    //------------------------------------------------------------
    // MultiSig : Domain registration process
    //------------------------------------------------------------
    /**
     * @dev Register an agenda to add a domain.
     * @param _domainAddress The address of the domain contract you want to register
     * @param _domainFee 마스크가 등록될 때, 도메인 컨트렉트에 할당되는 수수료
     *                   When the mask is registered, the fee assigned to the domain contract
     * @param _foundationFee 마스크가 등록될 때, APIS 재단에 할당되는 수수료
     *                       When a mask is registered, the fee assigned to the APIS Foundation
     * @param _needApproval True 일 경우, 소유자나 위임자 외에는 등록할 수 없어진다.
     *                      If TRUE, no one can register except the owner or delegate.
     * @param _isOpened True if the domain is available
     */
    function registerDomain(address _domainAddress, uint256 _domainFee, uint256 _foundationFee, bool _needApproval, bool _isOpened)
    public
    ownerExists(msg.sender)
    domainAddressNotExist(_domainAddress)
    returns (uint32 id)
    {
        id = domainRegistrationCount;

        Domain domainContract = Domain(_domainAddress);
        domainRegistrations[id] = DomainRegistration({
            domainAddress : _domainAddress,
            domainFee : _domainFee,
            foundationFee : _foundationFee,
            domainName : domainContract.getDomainName(),
            needApproval : _needApproval,
            isOpened : _isOpened,
            executed : false
            });

        domainRegistrationCount += 1;

        emit DomainRegistrationSubmission(id, _domainAddress, _domainFee, _foundationFee, domainContract.getDomainName(), _isOpened);

        confirmDomainRegistration(id);
    }

    function confirmDomainRegistration(uint32 _id)
    public
    ownerExists(msg.sender)
    domainRegistrationExist(_id)
    notConfirmedDomainRegistration(_id, msg.sender)
    notExecutedDomainRegistration(_id)
    {
        domainRegistrationConfirms[_id][msg.sender] = true;

        emit DomainRegistrationConfirmation(_id);

        executeDomainRegistration(_id);
    }

    function revokeDomainRegistrationConfirmation (uint32 _id)
    public
    ownerExists(msg.sender)
    confirmedDomainRegistration(_id, msg.sender)
    notExecutedDomainRegistration(_id)
    {
        domainRegistrationConfirms[_id][msg.sender] = false;

        emit DomainRegistrationRevocation(_id);
    }

    function executeDomainRegistration (uint32 _id)
    internal
    notExecutedDomainRegistration(_id)
    {
        if(isDomainRegistrationConfirmed(_id)) {
            DomainRegistration storage domainRegistration = domainRegistrations[_id];

            performDomainRegistration(
                domainRegistration.domainAddress,
                domainRegistration.domainName,
                domainRegistration.needApproval,
                domainRegistration.isOpened,
                domainRegistration.domainFee,
                domainRegistration.foundationFee);


            domainRegistrations[_id].executed = true;
            emit DomainRegistrationExecution(_id);
        }
    }

    function performDomainRegistration(address _domainAddress, string _domainName, bool _needApproval, bool _isOpened, uint256 _domainFee, uint256 _foundationFee)
    internal
    {
        DomainConfig memory domainConfig = DomainConfig({
            domainAddress : _domainAddress,
            domainName : _domainName,
            needApproval : _needApproval,
            isOpened : _isOpened,
            domainFee : _domainFee,
            foundationFee : _foundationFee
            });

        domainConfigs[_domainAddress] = domainConfig;
        domainContractAddresses.push(_domainAddress);
        isDomainRegistered[_domainAddress] = true;
        domainCount += 1;
    }


    function isDomainRegistrationConfirmed (uint32 _id)
    internal
    constant
    returns (bool)
    {
        uint32 count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (domainRegistrationConfirms[_id][owners[i]])
                count += 1;
            if (count == required)
                return true;
        }
    }


    function getCountOfDomainRegistrationConfirms (uint32 _id)
    public
    constant
    returns (uint256 count)
    {
        count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (domainRegistrationConfirms[_id][owners[i]])
                count += 1;
        }
    }






    //------------------------------------------------------------
    // MultiSig : Default fee change process
    //------------------------------------------------------------

    function registerDefaultFeeChange(uint256 _defaultFee)
    public
    ownerExists(msg.sender)
    returns (uint32 id)
    {
        id = defaultFeeChangeCount;
        defaultFeeChanges[id] = DefaultFeeChange({
            registered : true,
            defaultFee : _defaultFee,
            executed : false
            });

        defaultFeeChangeCount += 1;

        emit DefaultFeeChangeSubmission(id, _defaultFee);

        confirmDefaultFeeChange(id);
    }

    function confirmDefaultFeeChange(uint32 _id)
    public
    ownerExists(msg.sender)
    defaultFeeChangeExist(_id)
    notConfirmedDefaultFeeChange(_id, msg.sender)
    notExecutedDefaultFeeChange(_id)
    {
        defaultFeeChangeConfirms[_id][msg.sender] = true;

        emit DefaultFeeChangeConfirmation(_id);

        executeDefaultFeeChange(_id);
    }

    function revokeDefaultFeeChangeConfirmation (uint32 _id)
    public
    ownerExists(msg.sender)
    confirmedDefaultFeeChange(_id, msg.sender)
    notExecutedDefaultFeeChange(_id)
    {
        defaultFeeChangeConfirms[_id][msg.sender] = false;

        emit DefaultFeeChangeRevocation(_id);
    }

    function executeDefaultFeeChange (uint32 _id)
    public
    ownerExists(msg.sender)
    confirmedDefaultFeeChange(_id, msg.sender)
    notExecutedDefaultFeeChange(_id)
    {
        if(isDefaultFeeChangeConfirmed(_id)) {
            DefaultFeeChange storage defaultFeeChange = defaultFeeChanges[_id];

            defaultFeeChange.executed = true;

            defaultFee = defaultFeeChange.defaultFee;

            emit DefaultFeeChangeExecution(_id);
        }
    }

    function isDefaultFeeChangeConfirmed (uint32 _id)
    public
    constant
    returns (bool)
    {
        uint32 count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (defaultFeeChangeConfirms[_id][owners[i]])
                count += 1;
            if (count == required)
                return true;
        }
    }

    function getCountOfDefaultFeeChangeConfirms (uint32 _id)
    public
    constant
    returns (uint32 count)
    {
        count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (defaultFeeChangeConfirms[_id][owners[i]])
                count += 1;
        }
    }





    //------------------------------------------------------------
    // MultiSig : Domain fee rate of APIS foundation change process
    //------------------------------------------------------------

    function registerDomainConfigChange(uint _domainId, uint256 _domainFee, uint256 _foundationFee, bool _needApproval, bool _isOpened)
    public
    ownerExists(msg.sender)
    returns (uint32 id)
    {
        id = domainConfigChangeCount;
        domainConfigChanges[id] = DomainConfigChange({
            registered : true,
            domainId : _domainId,
            domainFee : _domainFee,
            foundationFee : _foundationFee,
            needApproval : _needApproval,
            isOpened : _isOpened,
            executed : false
            });

        domainConfigChangeCount += 1;

        emit DomainConfigChangeSubmission(id, _domainId, _domainFee, _foundationFee, _isOpened);

        confirmDomainConfigChange(id);
    }

    function confirmDomainConfigChange(uint32 _id)
    public
    ownerExists(msg.sender)
    domainConfigChangeExist(_id)
    notConfirmedDomainConfigChange(_id, msg.sender)
    notExecutedDomainConfigChange(_id)
    {
        domainConfigChangeConfirms[_id][msg.sender] = true;

        emit DomainConfigChangeConfirmation(_id);

        executeDomainConfigChange(_id);
    }

    function revokeDomainConfigChangeConfirmation (uint32 _id)
    public
    ownerExists(msg.sender)
    confirmedDomainConfigChange(_id, msg.sender)
    notExecutedDomainConfigChange(_id)
    {
        domainConfigChangeConfirms[_id][msg.sender] = false;

        emit DomainConfigChangeRevocation(_id);
    }

    function executeDomainConfigChange (uint32 _id)
    internal
    ownerExists(msg.sender)
    confirmedDomainConfigChange(_id, msg.sender)
    notExecutedDomainConfigChange(_id)
    {
        if(isDomainConfigChangeConfirmed(_id)) {
            DomainConfigChange storage config = domainConfigChanges[_id];

            config.executed = true;

            domainConfigs[domainContractAddresses[config.domainId]].domainFee = config.domainFee;
            domainConfigs[domainContractAddresses[config.domainId]].foundationFee = config.foundationFee;
            domainConfigs[domainContractAddresses[config.domainId]].isOpened = config.isOpened;


            emit DomainConfigChangeExecution(_id);
        }
    }

    function isDomainConfigChangeConfirmed (uint32 _id)
    internal
    constant
    returns (bool)
    {
        uint32 count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (domainConfigChangeConfirms[_id][owners[i]])
                count += 1;
            if (count == required)
                return true;
        }
    }

    function getCountOfDomainConfigChangeConfirms (uint32 _id)
    public
    constant
    returns (uint32 count)
    {
        count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (domainConfigChangeConfirms[_id][owners[i]])
                count += 1;
        }
    }
}