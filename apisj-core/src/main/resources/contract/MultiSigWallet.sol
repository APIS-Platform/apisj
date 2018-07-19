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
        ownerExists(msg.sender)
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

        for(uint i =0 ; i < owners.length; i++) {
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
}


contract MultisigApis is Owners {

    //@dev These events occur when the withdrawal agenda is registered / confirmed / revoked / executed.
    event WithdrawalSubmission(uint indexed withdrawalId, address to, uint256 attoApis);
    event WithdrawalConfirmation(address indexed owner, uint indexed withdrawalId);
    event WithdrawalRevocation(address indexed owner, uint indexed withdrawalId);
    event WithdrawalExecution(uint indexed withdrawalId);


    //@dev Owners can not register more than 50
    uint constant public MAX_OWNER_COUNT = 50;


    uint8[] public withdrawalList;
    mapping(uint => Withdrawal) public withdrawals;

    mapping(uint => mapping (address => bool)) public withdrawalConfirmations;



    struct Withdrawal {
        address to;
        uint attoApis;
        bool executed;
    }



    modifier validWithdrawalAmount(uint256 attoApis) {
        require(address(this).balance >= attoApis);
        _;
    }

    modifier withdrawalExists(uint _withdrawalId) {
        require(withdrawals[_withdrawalId].to != 0);
        _;
    }

    modifier confirmedWithdrawal(uint _withdrawalId, address _owner) {
        require(withdrawalConfirmations[_withdrawalId][_owner]);
        _;
    }

    modifier notConfirmedWithdrawal(uint _withdrawalId, address _owner) {
        require(!withdrawalConfirmations[_withdrawalId][_owner]);
        _;
    }

    modifier notExecutedWithdrawal(uint _withdrawalId) {
        require(!withdrawals[_withdrawalId].executed);
        _;
    }



    function() public payable {}


    /**
     * @dev Contract constructor sets initial owners and required number of confirmations.
     * @param _owners List of initial owners.
     * @param _required Number of required confirmations
     */
    constructor (address[] _owners, uint16 _required)
        public
        validRequirement(_owners.length, _required)
    {
        for (uint i = 0; i < _owners.length; i++) {
            isOwner[_owners[i]] = true;
        }

        owners = _owners;
        required = _required;
    }


    function balance() public constant returns (uint256 aApis) {
        return address(this).balance;
    }



    function countWithdrawal() public constant returns (uint256 count) {
        count = withdrawalList.length;
    }

    /**
     * @dev Allows an owner to submit and confirm a withdrawal
     */
    function registerWithdrawal(address _to, uint256 _attoApis)
        public
        notNull(_to)
        ownerExists(msg.sender)
        validWithdrawalAmount(_attoApis)
        returns (uint withdrawalId)
    {
        withdrawalId = withdrawalList.length;
        withdrawalList.push(1);
        withdrawals[withdrawalId] = Withdrawal({
            to : _to,
            attoApis : _attoApis,
            executed : false
        });

        emit WithdrawalSubmission(withdrawalId, _to, _attoApis);

        confirmWithdrawal(withdrawalId);
    }

    /**
     * @dev Allows an owner to confirm a withdrawal
     * @param _withdrawalId Withdrawal ID
     */
    function confirmWithdrawal(uint _withdrawalId)
        public
        ownerExists(msg.sender)
        withdrawalExists(_withdrawalId)
        notConfirmedWithdrawal(_withdrawalId, msg.sender)
    {
        withdrawalConfirmations[_withdrawalId][msg.sender] = true;
        emit WithdrawalConfirmation(msg.sender, _withdrawalId);

        executeWithdrawal(_withdrawalId);
    }


    /**
     * @dev Allows an owner to revoke a confirmation for a transaction
     * @param _withdrawalId Withdrawal ID
     */
    function revokeConfirmation(uint _withdrawalId)
        public
        ownerExists(msg.sender)
        confirmedWithdrawal(_withdrawalId, msg.sender)
        notExecutedWithdrawal(_withdrawalId)
    {
        withdrawalConfirmations[_withdrawalId][msg.sender] = false;
        emit WithdrawalRevocation(msg.sender, _withdrawalId);
    }


    /**
     * @dev Allows an owner to execute a confirmed withdrawal
     * @param _withdrawalId withdrawal ID
     */
    function executeWithdrawal(uint _withdrawalId)
        internal
        ownerExists(msg.sender)
        confirmedWithdrawal(_withdrawalId, msg.sender)
        notExecutedWithdrawal(_withdrawalId)
    {
        if(isWithdrawalConfirmed(_withdrawalId)) {
            Withdrawal storage withdrawal = withdrawals[_withdrawalId];
            withdrawal.to.transfer(withdrawal.attoApis);
            withdrawal.executed = true;

            emit WithdrawalExecution(_withdrawalId);
        }
    }

    /**
     * @dev Returns the confirmation status of a withdrawal.
     * @param _withdrawalId Withdrawal ID.
     * @return Confirmation status.
     */
    function isWithdrawalConfirmed(uint _withdrawalId)
        internal
        constant
        returns (bool)
    {
        uint count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (withdrawalConfirmations[_withdrawalId][owners[i]])
                count += 1;
            if (count == required)
                return true;
        }
    }
}