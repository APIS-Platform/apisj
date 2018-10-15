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


    mapping(uint32 => OwnerChange) public ownerChanges;
    mapping(uint32 => RequirementChange) public requirementChanges;

    mapping(uint32 => mapping (address => bool)) public ownerChangeConfirmations;
    mapping(uint32 => mapping (address => bool)) public requirementChangeConfirmations;

    mapping(address => bool) public isOwner;
    address[] public owners;
    uint16 public required;

    uint32 public requirementChangeCount;
    uint32 public ownerChangeCount;


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

    modifier ownerEnoughToRemove() {
        require(owners.length > 1);
        _;
    }

    modifier ownerNotEnoughToAdd() {
        require(owners.length < MAX_OWNER_COUNT);
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
    modifier confirmedOwnerChange(uint32 _changeId, address _owner) {
        require(ownerChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev "_owner" should not have confirmed the "_changeId" agenda.
     */
    modifier notConfirmedOwnerChange(uint32 _changeId, address _owner) {
        require(!ownerChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev The "_changeId" item should not have been executed.
     */
    modifier notExecutedOwnerChange(uint32 _changeId) {
        require(!ownerChanges[_changeId].executed);
        _;
    }



    /**
     * @dev "_owner" should confirm the "_changeId" agenda.
     */
    modifier confirmedRequirement(uint32 _changeId, address _owner) {
        require(requirementChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev "_owner" should not have confirmed the "_changeId" agenda.
     */
    modifier notConfirmedRequirement(uint32 _changeId, address _owner) {
        require(!requirementChangeConfirmations[_changeId][_owner]);
        _;
    }

    /**
     * @dev The "_changeId" item should not have been executed.
     */
    modifier notExecutedRequirement(uint32 _changeId) {
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
    ownerNotEnoughToAdd()
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
    ownerEnoughToRemove()
    returns (uint ownerChangeId)
    {
        return registerChangeOwner(_owner, false);
    }


    function registerChangeOwner(address _owner, bool _isAdd)
    internal
    ownerExists(msg.sender)
    returns (uint32 ownerChangeId)
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


    function confirmOwnerChange(uint32 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedOwnerChange(_changeId)
    notConfirmedOwnerChange(_changeId, msg.sender)
    {
        ownerChangeConfirmations[_changeId][msg.sender] = true;
        emit OwnerChangeConfirmation(msg.sender, _changeId);

        executeOwnerChange(_changeId);
    }

    function revokeOwnerChangeConfirmation(uint32 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedOwnerChange(_changeId)
    confirmedOwnerChange(_changeId, msg.sender)
    {
        ownerChangeConfirmations[_changeId][msg.sender] = false;
        emit OwnerChangeRevocation(msg.sender, _changeId);
    }

    function executeOwnerChange(uint32 _changeId)
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


    function isOwnerChangeConfirmed(uint32 _changeId)
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
    ownerNotEnoughToAdd()
    {
        isOwner[_owner] = true;
        owners.push(_owner);

        emit OwnerAddition(_owner);
    }


    function removeOwner(address _owner)
    internal
    ownerExists(_owner)
    ownerEnoughToRemove()
    {
        isOwner[_owner] = false;

        for(uint i =0 ; i < owners.length; i++) {
            if(owners[i] == _owner) {
                owners[i] = owners[owners.length - 1];
                break;
            }
        }

        owners.length -= 1;

        if(owners.length < required) {
            required = uint16(owners.length);
        }

        emit OwnerRemoval(_owner);
    }





    //------------------------------------------------------------
    // MultiSig : Requirement change process
    //------------------------------------------------------------
    function registerRequirementChange(uint16 _requirement)
    public
    ownerExists(msg.sender)
    validRequirement(owners.length, _requirement)
    returns (uint32 requirementChangeId)
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


    function confirmRequirementChange(uint32 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedRequirement(_changeId)
    notConfirmedRequirement(_changeId, msg.sender)
    validRequirement(owners.length, requirementChanges[_changeId].requirement)
    {
        requirementChangeConfirmations[_changeId][msg.sender] = true;
        emit RequirementChangeConfirmation(msg.sender, _changeId);

        executeRequirementChange(_changeId);
    }

    function revokeRequirementChangeConfirmation(uint32 _changeId)
    public
    ownerExists(msg.sender)
    notExecutedRequirement(_changeId)
    confirmedRequirement(_changeId, msg.sender)
    {
        requirementChangeConfirmations[_changeId][msg.sender] = false;
        emit RequirementChangeRevocation(msg.sender, _changeId);
    }


    function executeRequirementChange(uint32 _changeId)
    internal
    ownerExists(msg.sender)
    notExecutedRequirement(_changeId)
    confirmedRequirement(_changeId, msg.sender)
    validRequirement(owners.length, requirementChanges[_changeId].requirement)
    {
        if(isRequirementChangeConfirmed(_changeId)) {
            RequirementChange storage requirementChange = requirementChanges[_changeId];

            required = requirementChange.requirement;
            requirementChange.executed = true;

            emit RequirementChangeExecution(_changeId);
        }
    }


    function isRequirementChangeConfirmed(uint32 _changeId)
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







contract BuyMineral is Owners {

    event MNR(address buyer, uint256 attoApis, uint256 attoMnr);

    uint256 decimal = 18;

    // @dev Address of the Foundation to Manage Fees
    address foundationAccount;

    uint256[] apisUpperLimits;
    uint256[] exchangeRates;


    modifier emptyOwner () {
        require(owners.length == 0 && required == 0);
        _;
    }

    modifier hasFee () {
        require(address(this).balance > 0);
        _;
    }



    function init (address[] _owners, uint16 _required)
    public
    validRequirement(_owners.length, _required)
    emptyOwner() {
        for (uint i = 0; i < _owners.length; i++) {
            isOwner[_owners[i]] = true;
        }

        owners = _owners;
        required = _required;

        foundationAccount = 0x1000000000000000000000000000000000037448;


        apisUpperLimits.push(10*10**decimal);
        exchangeRates.push(110);

        apisUpperLimits.push(100*10**decimal);
        exchangeRates.push(120);

        apisUpperLimits.push(1000*10**decimal);
        exchangeRates.push(200);

        apisUpperLimits.push(10000*10**decimal);
        exchangeRates.push(400);

        apisUpperLimits.push(100000*10**decimal);
        exchangeRates.push(600);

        apisUpperLimits.push(500000*10**decimal);
        exchangeRates.push(800);

        apisUpperLimits.push(1000000*10**decimal);
        exchangeRates.push(1000);

        apisUpperLimits.push(10000000*10**decimal);
        exchangeRates.push(10000);
    }




    function () payable public {
        buyMNR();
    }

    function buyMNR() payable public {
        uint256 mnr = calcMNR(msg.value);

        emit MNR(msg.sender, msg.value, mnr);
    }




    function calcMNR(uint256 attoAmount)
    public
    view
    returns (uint256 mnr) {

        for(uint256 i = 0; i < apisUpperLimits.length ; i++) {
            if(attoAmount > apisUpperLimits[i]) {
                mnr = attoAmount*exchangeRates[i]/100;
                break;
            }
        }

        if(mnr == 0) {
            mnr = attoAmount;
        }
    }



    // TODO 수수료율을 변경하는 메서드를 추가해야함




    function withdrawal()
    public
    ownerExists(msg.sender)
    hasFee() {
        foundationAccount.transfer(address(this).balance);
    }
}