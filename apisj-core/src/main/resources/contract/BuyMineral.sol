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



/**
 * @title SafeMath
 * @dev Math operations with safety checks that throw on error
 */
library SafeMath {
    /**
    * @dev Multiplies two numbers, reverts on overflow.
    */
    function mul(uint256 a, uint256 b) internal pure returns (uint256) {
        // Gas optimization: this is cheaper than requiring 'a' not being zero, but the
        // benefit is lost if 'b' is also tested.
        // See: https://github.com/OpenZeppelin/openzeppelin-solidity/pull/522
        if (a == 0) {
            return 0;
        }

        uint256 c = a * b;
        require(c / a == b);

        return c;
    }

    /**
    * @dev Integer division of two numbers truncating the quotient, reverts on division by zero.
    */
    function div(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b > 0); // Solidity only automatically asserts when dividing by 0
        uint256 c = a / b;
        // assert(a == b * c + a % b); // There is no case in which this doesn't hold

        return c;
    }

    /**
    * @dev Subtracts two numbers, reverts on overflow (i.e. if subtrahend is greater than minuend).
    */
    function sub(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b <= a);
        uint256 c = a - b;

        return c;
    }

    /**
    * @dev Adds two numbers, reverts on overflow.
    */
    function add(uint256 a, uint256 b) internal pure returns (uint256) {
        uint256 c = a + b;
        require(c >= a);

        return c;
    }

    /**
    * @dev Divides two numbers and returns the remainder (unsigned integer modulo),
    * reverts when dividing by zero.
    */
    function mod(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b != 0);
        return a % b;
    }
}





contract BuyMineral is Owners {
    using SafeMath for uint256;

    event ExchangeRateChangeSubmission  (uint changeId, uint256[] apisUpperLimits, uint256[] exchangeRates);
    event ExchangeRateChangeConfirmation(address indexed owner, uint indexed changeId);
    event ExchangeRateChangeRevocation  (address indexed owner, uint indexed changeId);
    event ExchangeRateChangeExecution   (uint changeId, uint256[] apisUpperLimits, uint256[] exchangeRates);

    event MNR(address buyer, address beneficiary, uint256 attoApis, uint256 attoMnr);



    // @dev Address of the Foundation to Manage Fees
    address foundationAccount;

    uint256[] apisUpperLimits;
    uint256[] exchangeRates;

    uint32 public changeCount;
    mapping(uint => ExchangeRateChange) public exchangeRateChanges;
    mapping (uint256 => mapping (address => bool)) public exchangeRateChangeConfirms;

    struct ExchangeRateChange {
        uint256[] upperLimits;
        uint256[] rates;
        bool executed;
    }


    modifier emptyOwner () {
        require(owners.length == 0 && required == 0);
        _;
    }

    modifier hasFee () {
        require(address(this).balance > 0);
        _;
    }

    modifier isNotNullBeneficiary(address beneficiary) {
        require(beneficiary != 0x0);
        _;
    }



    modifier validExchangeRate (uint256[] _apisUpperLimits, uint256[] _exchangeRates) {
        require(_apisUpperLimits.length > 0 && _apisUpperLimits.length == _exchangeRates.length);

        for(uint256 i = 1; i < _apisUpperLimits.length; i++) {
            require(_apisUpperLimits[i] > _apisUpperLimits[i - 1]);
        }
        _;
    }

    modifier exchangeRateChangeExists(uint _changeId) {
        require(exchangeRateChanges[_changeId].upperLimits.length > 0);
        _;
    }

    modifier confirmedExchangeRateChange(uint _changeId, address _owner) {
        require(exchangeRateChangeConfirms[_changeId][_owner]);
        _;
    }

    modifier notConfirmedExchangeRateChange(uint _changeId, address _owner) {
        require(!exchangeRateChangeConfirms[_changeId][_owner]);
        _;
    }

    modifier notExecutedExchangeRateChange(uint _changeId) {
        require(!exchangeRateChanges[_changeId].executed);
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


        apisUpperLimits.push(uint256(10).mul(1000000000000000000));
        exchangeRates.push(105);

        apisUpperLimits.push(uint256(100).mul(1000000000000000000));
        exchangeRates.push(110);

        apisUpperLimits.push(uint256(1000).mul(1000000000000000000));
        exchangeRates.push(120);

        apisUpperLimits.push(uint256(10000).mul(1000000000000000000));
        exchangeRates.push(150);

        apisUpperLimits.push(uint256(100000).mul(1000000000000000000));
        exchangeRates.push(200);

        apisUpperLimits.push(uint256(500000).mul(1000000000000000000));
        exchangeRates.push(300);

        apisUpperLimits.push(uint256(1000000).mul(1000000000000000000));
        exchangeRates.push(500);

        apisUpperLimits.push(uint256(10000000).mul(1000000000000000000));
        exchangeRates.push(1000);


    }




    function () payable public {
        buyMNR(msg.sender);
    }

    function buyMNR(address beneficiary)
    payable
    public
    isNotNullBeneficiary(beneficiary)
    {
        uint256 mnr = calcMNR(msg.value);

        // overflow check
        require(mnr >= msg.value);

        emit MNR(msg.sender, beneficiary, msg.value, mnr);
    }





    function calcMNR(uint256 attoAmount)
    public
    view
    returns (uint256 mnr) {

        for(uint256 i = 0; i < apisUpperLimits.length ; i++) {
            if(attoAmount >= apisUpperLimits[i]) {
                mnr = attoAmount.mul(exchangeRates[i]).div(uint256(100));
            }
        }

        if(mnr == 0) {
            mnr = attoAmount;
        }
    }



    function registerExchangeRateChange(uint256[] _apisUpperLimits, uint256[] _exchangeRates)
    public
    ownerExists(msg.sender)
    validExchangeRate(_apisUpperLimits, _exchangeRates)
    returns (uint256 changeId)
    {
        changeId = changeCount;

        exchangeRateChanges[changeId] = ExchangeRateChange({
            upperLimits : _apisUpperLimits,
            rates : _exchangeRates,
            executed : false
            });

        changeCount += 1;

        emit ExchangeRateChangeSubmission(changeId, apisUpperLimits, exchangeRates);

        confirmExchangeRateChange(changeId);
    }


    function confirmExchangeRateChange(uint256 _changeId)
    public
    ownerExists(msg.sender)
    exchangeRateChangeExists(_changeId)
    notConfirmedExchangeRateChange(_changeId, msg.sender)
    {
        exchangeRateChangeConfirms[_changeId][msg.sender] = true;
        emit ExchangeRateChangeConfirmation(msg.sender, _changeId);

        executeExchangeRateChange(_changeId);
    }


    function revokeExchangeRateChangeConfirmation(uint256 _changeId)
    public
    ownerExists(msg.sender)
    confirmedExchangeRateChange(_changeId, msg.sender)
    notExecutedExchangeRateChange(_changeId)
    {
        exchangeRateChangeConfirms[_changeId][msg.sender] = false;
        emit ExchangeRateChangeRevocation(msg.sender, _changeId);
    }


    function executeExchangeRateChange(uint256 _changeId)
    internal
    ownerExists(msg.sender)
    confirmedExchangeRateChange(_changeId, msg.sender)
    validExchangeRate(exchangeRateChanges[_changeId].upperLimits, exchangeRateChanges[_changeId].rates)
    notExecutedExchangeRateChange(_changeId)
    {
        if(isExchangeRateChangeConfirmed(_changeId)) {
            ExchangeRateChange storage exchangeRateChange = exchangeRateChanges[_changeId];
            changeExchangeRate(exchangeRateChange.upperLimits, exchangeRateChange.rates);
            exchangeRateChange.executed = true;

            emit ExchangeRateChangeExecution(_changeId, exchangeRateChange.upperLimits, exchangeRateChange.rates);
        }
    }

    function isExchangeRateChangeConfirmed(uint256 _changeId)
    internal
    constant
    returns (bool)
    {
        uint256 count = 0;
        for (uint256 i = 0; i < owners.length; i++) {
            if(exchangeRateChangeConfirms[_changeId][owners[i]]) {
                count += 1;
            }

            if(count == required) {
                return true;
            }
        }

        return false;
    }




    function changeExchangeRate(uint256[] _apisUpperLimits, uint256[] _exchangeRates)
    internal
    {
        require(_apisUpperLimits.length > 0);
        require(_apisUpperLimits.length == _exchangeRates.length);

        for(uint256 i = 0; i < _apisUpperLimits.length; i++) {
            apisUpperLimits[i] = _apisUpperLimits[i];
            exchangeRates[i] = _exchangeRates[i];

            if(i > 0) {
                assert(apisUpperLimits[i] > apisUpperLimits[i - 1]);
            }
        }
        apisUpperLimits.length = _apisUpperLimits.length;
    }




    function withdrawal()
    public
    ownerExists(msg.sender)
    hasFee() {
        foundationAccount.transfer(address(this).balance);
    }
}