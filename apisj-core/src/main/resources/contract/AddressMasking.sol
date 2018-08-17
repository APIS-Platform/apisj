    pragma solidity ^0.4.15;

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













    /**
     *
     */
    contract Domain is Owners {
        using strings for *;


        event MaskPendingSubmission (uint indexed pendingId, address indexed face, string name);
        event MaskApprovalConfirmation (uint indexed pendingId);
        event MaskApprovalRevocation (uint indexed pendingId);
        event MaskApprovalExecution(uint indexed pendingId);

        event ApprovalDelegatorChangeSubmission (uint indexed id, address delegator);
        event ApprovalDelegatorChangeConfirmation (uint indexed id);
        event ApprovalDelegatorChangeRevocation (uint indexed id);
        event ApprovalDelegatorChangeExecution(uint indexed id);

        event FeeChangeSubmission (uint indexed id, uint256 fee);
        event FeeChangeConfirmation (uint indexed id);
        event FeeChangeRevocation (uint indexed id);
        event FeeChangeExecution(uint indexed id);

        event NeedApprovalChangeSubmission (uint indexed id, bool needed);
        event NeedApprovalChangeConfirmation (uint indexed id);
        event NeedApprovalChangeRevocation (uint indexed id);
        event NeedApprovalChangeExecution(uint indexed id);

        event WithdrawalSubmission (uint indexed id, uint256 aApis);
        event WithdrawalConfirmation (uint indexed id);
        event WithdrawalRevocation (uint indexed id);
        event WithdrawalExecution(uint indexed id, uint256 aApis);




        AddressMasking addressMasking;

        // @dev Name after "@"
        string public domainName;

        // @dev Maximum length of the name to the left of "@", length by RFC 2822
        uint constant public maxNameLength = 64;

        // @dev Fees required to register a domain
        uint256 public registerFee;

        // @dev TRUE if approval is required when registering a name in the domain
        bool public needApproval;

        // @dev Addresses registered in this domain
        address[] public registeredMasks;

        // @dev Addresses waiting for registration approval
        mapping(address => string) public pendingMasks;

        // @dev Number of addresses waiting for registration approval
        uint public pendingCount;

        // @dev The total sum of the fees paid when the name is registered in the domain. Withdrawable.
        //uint256 public totalPaidFee;


        // @dev This address can handle approval by itself. It is assigned through the vote of several owners.
        address public approvalDelegator;


        // Multisig
        mapping(uint => ApprovalDelegator) public approvalDelegatorChanges;
        mapping(uint => MaskPending) public maskPendings;
        mapping(uint => RegistrationFee) public feeChanges;
        mapping(uint => NeedApproval) public needApprovalChanges;
        mapping(uint => Withdrawal) public withdrawals;

        mapping(uint => mapping(address => bool)) public approvalDelegatorChangeConfirms;
        mapping(uint => mapping(address => bool)) public maskApprovalConfirms;
        mapping(uint => mapping(address => bool)) public feeChangeConfirms;
        mapping(uint => mapping(address => bool)) public needApprovalChangeConfirms;
        mapping(uint => mapping(address => bool)) public withdrawalConfirms;

        uint public approvalDelegatorChangeCount;
        uint public maskPendingCount;
        uint public feeChangeCount;
        uint public needApprovalChangeCount;
        uint public withdrawalCount;


        struct ApprovalDelegator {
            address delegator;
            bool executed;
        }

        struct MaskPending {
            address face;
            string name;
            bool executed;
        }

        struct RegistrationFee {
            bool registered;
            uint256 fee;
            bool executed;
        }

        struct NeedApproval {
            bool registered;
            bool needApproval;
            bool executed;
        }

        struct Withdrawal {
            bool registered;
            address receiver;
            uint256 aApis;
            bool executed;
        }




        modifier validDomainNameCharacter(string name) {
            require(name.toSlice().find("@".toSlice()).len() == 0);
            _;
        }

        modifier validFee(uint256 fee) {
            require(fee >= registerFee);
            _;
        }

        modifier validNameLength(string name) {
            require(name.toSlice().len() <= maxNameLength);
            _;
        }


        modifier isApprovalDelegator(address who) {
            require(approvalDelegator == who);
            _;
        }

        modifier hasApprovalAuthority (address who) {
            require(isOwner[who] || approvalDelegator == who);
            _;
        }

        modifier fromAddressMaskingContract(address maskingContract) {
            require(addressMasking == address(maskingContract));
            _;
        }




        modifier maskPendingExist(uint _id) {
            require(maskPendings[_id].face != 0);
            _;
        }

        modifier notConfirmedPendingMask (uint _id, address _owner) {
            require(maskApprovalConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedPendingMask (uint _id, address _owner) {
            require(maskApprovalConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedApprovealMask(uint _id) {
            require(maskPendings[_id].executed == false);
            _;
        }




        modifier approvalDelegatorChangeExist(uint _id) {
            require(approvalDelegatorChanges[_id].delegator != 0);
            _;
        }

        modifier notConfirmedApprovalDelegatorChange (uint _id, address _owner) {
            require(approvalDelegatorChangeConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedApprovalDelegatorChange (uint _id, address _owner) {
            require(approvalDelegatorChangeConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedApprovalDelegatorChange (uint _id) {
            require(approvalDelegatorChanges[_id].executed == false);
            _;
        }




        modifier feeChangeExist(uint _id) {
            require(feeChanges[_id].registered == true);
            _;
        }

        modifier notConfirmedFeeChange (uint _id, address _owner) {
            require(feeChangeConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedFeeChange (uint _id, address _owner) {
            require(feeChangeConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedFeeChange (uint _id) {
            require(feeChanges[_id].executed == false);
            _;
        }




        modifier needApprovalChangeExist(uint _id) {
            require(needApprovalChanges[_id].registered == true);
            _;
        }

        modifier notConfirmedNeedApprovalChange (uint _id, address _owner) {
            require(needApprovalChangeConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedneedApprovalChange (uint _id, address _owner) {
            require(needApprovalChangeConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedNeedApprovalChange (uint _id) {
            require(needApprovalChanges[_id].executed == false);
            _;
        }



        modifier validWithdrawalAmount() {
            require(address(this).balance > 0);
            _;
        }

        modifier withdrawalExist(uint _id) {
            require(withdrawals[_id].registered == true);
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
            uint256     _registerFee,
            bool        _needApproval,
            address     _addressMaskingContract,
            address[]   _domainOwners,
            uint16        _required
        )
            public
            validDomainNameCharacter(_domainName)
            validRequirement(_domainOwners.length, _required)
        {
            domainName = _domainName;
            registerFee = _registerFee;
            needApproval = _needApproval;
            owners = _domainOwners;
            required = _required;
            addressMasking = AddressMasking(_addressMaskingContract);
        }





        function getDomainName()
            external
            view
            returns (string)
        {
            return domainName;
        }

        function isRequiredApproval()
            external
            view
            returns (bool)
        {
            return needApproval;
        }

        function getRegistrationFee()
            external
            view
            returns (uint256)
        {
            return registerFee;
        }






        function registerPendingFace(address _face, string _name)
            external
            payable
            notNull(_face)
            validNameLength(_name)
            validFee(msg.value)
            fromAddressMaskingContract(msg.sender) // AddressMasking 주소에서만 허용해야한다
            returns (uint pendingId)
        {
            // If more than the fee is paid, the remaining APIS will return.
            uint256 refundValue = msg.value - registerFee;
            if(refundValue > 0) {
                _face.transfer(refundValue);
            }

            pendingId = maskPendingCount;
            maskPendings[pendingId] = MaskPending({
                face : _face,
                name : _name,
                executed : false
            });

            maskPendingCount += 1;

            emit MaskPendingSubmission(pendingId, _face, _name);

            confirmMaskApproval(pendingId);
        }

        function confirmMaskApproval(uint _pendingId)
            public
            ownerExists(msg.sender)
            maskPendingExist(_pendingId)
            notConfirmedPendingMask(_pendingId, msg.sender)
        {
            maskApprovalConfirms[_pendingId][msg.sender] = true;
            emit MaskApprovalConfirmation(_pendingId);

            executeMaskApproval(_pendingId);
        }

        function revokeMaskApprovalConfirmation (uint _pendingId)
            public
            ownerExists(msg.sender)
            confirmedPendingMask(_pendingId, msg.sender)
            notExecutedApprovealMask(_pendingId)
        {
            maskApprovalConfirms[_pendingId][msg.sender] = false;
            emit MaskApprovalRevocation(_pendingId);
        }

        function executeMaskApproval (uint _pendingId)
            public
            ownerExists(msg.sender)
            confirmedPendingMask(_pendingId, msg.sender)
            notExecutedApprovealMask(_pendingId)
        {
            if(isMaskApprovalConfirmed(_pendingId)) {
                approveMask(_pendingId);
            }
        }

        function isMaskApprovalConfirmed (uint _pendingId)
            public
            constant
            returns (bool)
        {
            uint count = 0;
            for (uint i = 0; i < owners.length; i++) {
                if (maskApprovalConfirms[_pendingId][owners[i]])
                    count += 1;
                if (count == required)
                    return true;
            }
        }

        function approveMask (uint _pendingId)
            internal
            hasApprovalAuthority(msg.sender)
        {
            MaskPending storage maskPending = maskPendings[_pendingId];

            // Register the address through the AddressMasking contract.
            addressMasking.putOnAMaskByDomain(maskPending.face, maskPending.name);

            maskPending.executed = true;

            emit MaskApprovalExecution(_pendingId);
        }

        /**
         * @dev Administrators who have been delegated the authority to approve name registration can immediately approve.
         * @param _pendingId The order of names waiting to be registered in the domain.
         */
        function approveMaskByDelegator(uint _pendingId)
            public
            isApprovalDelegator(msg.sender)
            notExecutedApprovealMask(_pendingId)
        {
            approveMask(_pendingId);
        }












        function registerApprovalDelegatorChange(address _delegator)
            public
            ownerExists(msg.sender)
            returns (uint id)
        {
            id = approvalDelegatorChangeCount;
            approvalDelegatorChanges[id] = ApprovalDelegator({
                delegator : _delegator,
                executed : false
            });

            approvalDelegatorChangeCount += 1;

            emit ApprovalDelegatorChangeSubmission(id, _delegator);

            confirmApprovalDelegatorChange(id);
        }

        function confirmApprovalDelegatorChange(uint _id)
            public
            ownerExists(msg.sender)
            approvalDelegatorChangeExist(_id)
            notConfirmedApprovalDelegatorChange(_id, msg.sender)
        {
            approvalDelegatorChangeConfirms[_id][msg.sender] = true;

            emit ApprovalDelegatorChangeConfirmation(_id);

            executeApprovalDelegatorChange(_id);
        }

        function revokeApprovalDelegatorChangeConfirmation (uint _id)
            public
            ownerExists(msg.sender)
            confirmedApprovalDelegatorChange(_id, msg.sender)
            notExecutedApprovalDelegatorChange(_id)
        {
            approvalDelegatorChangeConfirms[_id][msg.sender] = false;

            emit ApprovalDelegatorChangeRevocation(_id);
        }

        function executeApprovalDelegatorChange (uint _id)
            public
            ownerExists(msg.sender)
            confirmedApprovalDelegatorChange(_id, msg.sender)
            notExecutedApprovalDelegatorChange(_id)
        {
            if(isApprovalDelegatorChangeConfirmed(_id)) {
                ApprovalDelegator storage approvalDelegatorChange = approvalDelegatorChanges[_id];

                approvalDelegator = approvalDelegatorChange.delegator;
                approvalDelegatorChange.executed = true;

                emit ApprovalDelegatorChangeExecution(_id);
            }
        }

        function isApprovalDelegatorChangeConfirmed (uint _id)
            public
            constant
            returns (bool)
        {
            uint count = 0;
            for (uint i = 0; i < owners.length; i++) {
                if (approvalDelegatorChangeConfirms[_id][owners[i]])
                    count += 1;
                if (count == required)
                    return true;
            }
        }












        function registerFeeChange(uint256 _fee)
            public
            ownerExists(msg.sender)
            returns (uint feeChangeId)
        {
            feeChangeId = feeChangeCount;
            feeChanges[feeChangeId] = RegistrationFee({
                registered : true,
                fee : _fee,
                executed : false
            });

            feeChangeCount += 1;

            emit FeeChangeSubmission(feeChangeId, _fee);

            confirmFeeChange(feeChangeId);
        }

        function confirmFeeChange(uint _id)
            public
            ownerExists(msg.sender)
            feeChangeExist(_id)
            notConfirmedFeeChange(_id, msg.sender)
        {
            feeChangeConfirms[_id][msg.sender] = true;

            emit FeeChangeConfirmation(_id);

            executeFeeChange(_id);
        }

        function revokeFeeChangeConfirmation (uint _id)
            public
            ownerExists(msg.sender)
            confirmedFeeChange(_id, msg.sender)
            notExecutedFeeChange(_id)
        {
            feeChangeConfirms[_id][msg.sender] = false;

            emit FeeChangeRevocation(_id);
        }

        function executeFeeChange (uint _id)
            public
            ownerExists(msg.sender)
            confirmedFeeChange(_id, msg.sender)
            notExecutedFeeChange(_id)
        {
            if(isFeeChangeConfirmed(_id)) {
                RegistrationFee storage registrationFee = feeChanges[_id];

                registerFee = registrationFee.fee;
                registrationFee.executed = true;

                emit FeeChangeExecution(_id);
            }
        }

        function isFeeChangeConfirmed (uint _id)
            public
            constant
            returns (bool)
        {
            uint count = 0;
            for (uint i = 0; i < owners.length; i++) {
                if (feeChangeConfirms[_id][owners[i]])
                    count += 1;
                if (count == required)
                    return true;
            }
        }












        function registerNeedApprovalChange(bool _needed)
            public
            ownerExists(msg.sender)
            returns (uint id)
        {
            id = needApprovalChangeCount;
            needApprovalChanges[id] = NeedApproval({
                registered : true,
                needApproval : _needed,
                executed : false
            });

            needApprovalChangeCount += 1;

            emit NeedApprovalChangeSubmission(id, _needed);

            confirmNeedApprovalChange(id);
        }

        function confirmNeedApprovalChange(uint _id)
            public
            ownerExists(msg.sender)
            needApprovalChangeExist(_id)
            notConfirmedNeedApprovalChange(_id, msg.sender)
        {
            needApprovalChangeConfirms[_id][msg.sender] = true;

            emit NeedApprovalChangeConfirmation(_id);

            executeNeedApprovalChange(_id);
        }

        function revokeNeedApprovalChangeConfirmation (uint _id)
            public
            ownerExists(msg.sender)
            confirmedneedApprovalChange(_id, msg.sender)
            notExecutedNeedApprovalChange(_id)
        {
            needApprovalChangeConfirms[_id][msg.sender] = false;

            emit NeedApprovalChangeRevocation(_id);
        }

        function executeNeedApprovalChange (uint _id)
            public
            ownerExists(msg.sender)
            confirmedneedApprovalChange(_id, msg.sender)
            notExecutedNeedApprovalChange(_id)
        {
            if(isFeeChangeConfirmed(_id)) {
                NeedApproval storage needApprovalChange = needApprovalChanges[_id];

                needApproval = needApprovalChange.needApproval;
                needApprovalChange.executed = true;

                emit NeedApprovalChangeExecution(_id);
            }
        }

        function isNeedApprovalChangeConfirmed (uint _id)
            public
            constant
            returns (bool)
        {
            uint count = 0;
            for (uint i = 0; i < owners.length; i++) {
                if (needApprovalChangeConfirms[_id][owners[i]])
                    count += 1;
                if (count == required)
                    return true;
            }
        }












        function registerWithdrawal(address _receiver)
            public
            ownerExists(msg.sender)
            validWithdrawalAmount
            returns (uint id)
        {
            id = withdrawalCount;
            withdrawals[id] = Withdrawal({
                registered : true,
                aApis : address(this).balance,
                receiver : _receiver,
                executed : false
            });

            withdrawalCount += 1;

            emit WithdrawalSubmission(id, address(this).balance);

            confirmWithdrawal(id);
        }

        function confirmWithdrawal(uint _id)
            public
            ownerExists(msg.sender)
            withdrawalExist(_id)
            notConfirmedWithdrawal(_id, msg.sender)
        {
            withdrawalConfirms[_id][msg.sender] = true;

            emit WithdrawalConfirmation(_id);

            executeWithdrawal(_id);
        }

        function revokeWithdrawalConfirmation (uint _id)
            public
            ownerExists(msg.sender)
            confirmedWithdrawal(_id, msg.sender)
            notExecutedWithdrawal(_id)
        {
            withdrawalConfirms[_id][msg.sender] = false;

            emit WithdrawalRevocation(_id);
        }

        function executeWithdrawal (uint _id)
            public
            ownerExists(msg.sender)
            confirmedWithdrawal(_id, msg.sender)
            notExecutedWithdrawal(_id)
        {
            if(isWithdrawalConfirmed(_id)) {
                Withdrawal storage withdrawal = withdrawals[_id];

                require(withdrawal.aApis <= address(this).balance);

                address receiver = withdrawal.receiver;
                receiver.transfer(withdrawal.aApis);

                emit WithdrawalExecution(_id, withdrawal.aApis);
            }
        }

        function isWithdrawalConfirmed (uint _id)
            public
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



        event MaskAddition (address indexed face, string indexed mask);

        event DomainRegistrationSubmission  (uint indexed domainRegistrationId, address indexed domainAddress, uint256 domainFeeRateOfFoundation, string domainName, bool isOpened);
        event DomainRegistrationConfirmation(uint indexed domainRegistrationId);
        event DomainRegistrationRevocation  (uint indexed domainRegistrationId);
        event DomainRegistrationExecution   (uint indexed domainRegistrationId);

        event DefaultFeeChangeSubmission    (uint indexed defaultFeeChangeId, uint indexed aApis);
        event DefaultFeeChangeConfirmation  (uint indexed defaultFeeChangeId);
        event DefaultFeeChangeRevocation    (uint indexed defaultFeeChangeId);
        event DefaultFeeChangeExecution     (uint indexed defaultFeeChangeId);

        event FoundationAccountChangeSubmission   (uint indexed foundationAccountChangeId, address indexed foundationAccount);
        event FoundationAccountChangeConfirmation (uint indexed foundationAccountChangeId);
        event FoundationAccountChangeRevocation   (uint indexed foundationAccountChangeId);
        event FoundationAccountChangeExecution    (uint indexed foundationAccountChangeId);

        event DomainStateChangeSubmission   (uint indexed domainStateChangeId, uint domainId, bool indexed isOpened);
        event DomainStateChangeConfirmation (uint indexed domainStateChangeId);
        event DomainStateChangeRevocation   (uint indexed domainStateChangeId);
        event DomainStateChangeExecution    (uint indexed domainStateChangeId);

        event DomainFeeRateChangeSubmission   (uint indexed domainFeeRateChangeId, uint indexed domainId, uint256 indexed domainFeeRate);
        event DomainFeeRateChangeConfirmation (uint indexed domainFeeRateChangeId);
        event DomainFeeRateChangeRevocation   (uint indexed domainFeeRateChangeId);
        event DomainFeeRateChangeExecution    (uint indexed domainFeeRateChangeId);



        uint constant public DECIMAL = 18;

        // @dev Maximum length of the name to the left of "@", length by RFC 2822
        uint constant public MAX_NAME_LENGTH = 64;

        // @dev If the fee is free, some attacker may generate a lot of transactions and attack the network.
        uint256 public defaultFee = 100*(10**uint256(DECIMAL));



        mapping(address => bytes32) public masks;
        mapping(bytes32 => address) public faces;

        address[] public domainContractAddresses;
        mapping(address => bool) public isDomainRegistered;
        mapping(address => DomainConfig) public domainConfigs;


        // MultiSig
        mapping(uint32 => DomainRegistration) public domainRegistrations;
        mapping(uint32 => DefaultFeeChange) public defaultFeeChanges;
        mapping(uint32 => FoundationAccountChange) public foundationAccountChanges;
        mapping(uint32 => DomainStateChange) public domainStateChanges;
        mapping(uint32 => DomainFeeRateChange) public domainFeeRateChanges;
        mapping(uint => Withdrawal) public withdrawals;

        mapping(uint32 => mapping(address => bool)) public domainRegistrationConfirms;
        mapping(uint32 => mapping(address => bool)) public defaultFeeChangeConfirms;
        mapping(uint32 => mapping(address => bool)) public foundationAccountChangeConfirms;
        mapping(uint32 => mapping(address => bool)) public domainStateChangeConfirms;
        mapping(uint32 => mapping(address => bool)) public domainFeeRateChangeConfirms;
        mapping(uint => mapping(address => bool)) public withdrawalConfirms;

        uint32 public domainCount;
        uint32 public domainRegistrationCount;
        uint32 public defaultFeeChangeCount;
        uint32 public foundationAccountChangeCount;
        uint32 public domainStateChangeCount;
        uint32 public domainFeeRateChangeCount;
        uint public withdrawalCount;





        struct DomainConfig {
            address domainAddress;
            string domainName;
            bool isOpened;
            uint256 domainFeeRateOfFoundation;
        }

        struct DomainRegistration {
            address domainAddress;
            uint256 domainFeeRateOfFoundation;
            string domainName;
            bool isOpened;
            bool executed;
        }

        struct DefaultFeeChange {
            bool registered;
            uint256 defaultFee;
            bool executed;
        }

        struct FoundationAccountChange {
            address foundationAccount;
            bool executed;
        }

        struct DomainStateChange {
            uint64 domainId;
            bool registered;
            bool isOpened;
            bool executed;
        }

        struct DomainFeeRateChange {
            uint256 feeRate;
            uint64 domainId;
            bool registered;
            bool executed;
        }

        struct Withdrawal {
            bool registered;
            address receiver;
            uint256 aApis;
            bool executed;
        }






        modifier faceExist(address face) {
            require(masks[face] != 0x0);
            _;
        }

        modifier faceDoesNotExist(address face) {
            require(masks[face] == 0x0);
            _;
        }

        modifier validNameLength(string name) {
            require(name.toSlice().len() <= MAX_NAME_LENGTH);
            _;
        }

        modifier validMaskingFee(uint256 fee, uint32 domainId) {
            Domain domainContract = Domain(domainContractAddresses[domainId]);
            require(defaultFee + domainContract.getRegistrationFee() <= fee);
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

        modifier domainIdOpen(uint32 domainId) {
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






        modifier foundationAccountChangeExist(uint32 _id) {
            require(foundationAccountChanges[_id].foundationAccount != 0x0);
            _;
        }

        modifier notConfirmedFoundationAccountChange (uint32 _id, address _owner) {
            require(foundationAccountChangeConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedFoundationAccountChange (uint32 _id, address _owner) {
            require(foundationAccountChangeConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedFoundationAccountChange (uint32 _id) {
            require(foundationAccountChanges[_id].executed == false);
            _;
        }






        modifier domainStateChangeExist(uint32 _id) {
            require(domainStateChanges[_id].registered == true);
            _;
        }

        modifier notConfirmedDomainStateChange (uint32 _id, address _owner) {
            require(domainStateChangeConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedDomainStateChange (uint32 _id, address _owner) {
            require(domainStateChangeConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedDomainStateChange (uint32 _id) {
            require(domainStateChanges[_id].executed == false);
            _;
        }






        modifier domainFeeRateChangeExist(uint32 _id) {
            require(domainFeeRateChanges[_id].registered == true);
            _;
        }

        modifier notConfirmedDomainFeeRateChange (uint32 _id, address _owner) {
            require(domainFeeRateChangeConfirms[_id][_owner] == false);
            _;
        }

        modifier confirmedDomainFeeRateChange (uint32 _id, address _owner) {
            require(domainFeeRateChangeConfirms[_id][_owner] == true);
            _;
        }

        modifier notExecutedDomainFeeRateChange (uint32 _id) {
            require(domainFeeRateChanges[_id].executed == false);
            _;
        }



        modifier validWithdrawalAmount() {
            require(address(this).balance > 0);
            _;
        }

        modifier withdrawalExist(uint _id) {
            require(withdrawals[_id].registered == true);
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






        /**
         * @dev "@" is used to separate name and domain, so "@" is not allowed in names.
         */
        modifier validNameCharacter(string name) {
            require(name.toSlice().find("@".toSlice()).len() == 0);
            _;
        }




        /**
         * @dev Contract constructor sets initial owners and required number of confirmations.
         * @param _owners List of initial owners.
         * @param _required Number of required confirmations.
         */
        constructor (address[] _owners, uint16 _required, uint256 _defaultFee)
            public
            validRequirement(_owners.length, _required)
        {
            for (uint i = 0; i < _owners.length; i++) {
                isOwner[_owners[i]] = true;
            }
            
            owners = _owners;
            required = _required;
            defaultFee = _defaultFee;
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
            Domain domainContract = Domain(domainContractAddresses[_domainId]);
            registrationFee = defaultFee + domainContract.getRegistrationFee();
        }


        /**
         * @dev
         */
        function registerMask (address _face, string _name, uint32 _domainId)
            public
            payable
            domainIdExist(_domainId)
            domainIdOpen(_domainId)
            validMaskingFee(msg.value, _domainId)
            validNameLength(_name)
            validNameCharacter(_name)
            faceDoesNotExist(_face)
        {
            address domainAddress = domainContractAddresses[_domainId];
            Domain domainContract = Domain(domainAddress);

            // If more than the fee is paid, the remaining APIS will return.
            require(msg.value >= domainContract.getRegistrationFee() + defaultFee);
            uint256 refundValue = msg.value - (domainContract.getRegistrationFee() + defaultFee);

            if(refundValue > 0) {
                _face.transfer(refundValue);
            }

            // Transfer the registration fee to the contract, excluding the Foundation's portion.
            domainAddress.transfer(calcDomainFee(domainContract.getRegistrationFee(), _domainId));


            // 승인 절차가 있으면 도메인의 대기열에 추가만..
            if(domainContract.isRequiredApproval() == true) {
                domainContract.registerPendingFace(_face, _name);
            }

            // 도메인에 승인 절차가 없으면 바로 등록한다
            else {
                putOnAMask(_face, _name, address(domainContract));
            }
        }

        /**
         * @dev Calculate the registration fee, excluding the Foundation's portion.
         */
        function calcDomainFee(uint256 registrationFee, uint32 domainId)
            internal
            view
            returns(uint256)
        {
            uint256 foundationFeeRate = domainConfigs[domainContractAddresses[domainId]].domainFeeRateOfFoundation;
            return registrationFee - (registrationFee * (10**(DECIMAL - 1)) / foundationFeeRate);
        }


        /**
         * @dev This function is called from the Domain contract.
         */
        function putOnAMaskByDomain(address _face, string _name)
            external
            domainAddressExist(msg.sender)
            domainAddressOpen(msg.sender)
        {
            putOnAMask(_face, _name, msg.sender);
        }


        function putOnAMask (address _face, string _name, address _domainAddress)
            internal
        {
            string memory domainName = domainConfigs[_domainAddress].domainName;
            string memory mask = _name.toSlice().concat("@".toSlice()).toSlice().concat(domainName.toSlice());

            bytes32 maskHash = keccak256(bytes(mask));

            masks[_face] = maskHash;
            faces[maskHash] = _face;

            emit MaskAddition(_face, mask);
        }



        function getFace (string _mask)
            external
            view
            returns (address face)
        {
            bytes32 maskHash = keccak256(bytes(_mask));
            face = faces[maskHash];
        }


        function getMask (address _face)
            external
            view
            returns (bytes32 maskHash)
        {
            maskHash = masks[_face];
        }







        //------------------------------------------------------------
        // MultiSig : Domain registration process
        //------------------------------------------------------------
        /**
         * @dev Register an agenda to add a domain.
         * @param _domainAddress The address of the domain contract you want to register
         * @param _domainFeeRateOfFoundation Fee rate taken by APIS foundation when registering an address on a domain (30% = 30*(10**16), 100% = 10**18)
         * @param _domainName The name of the domain to register (@ Is not included)
         */
        function registerDomain(address _domainAddress, uint256 _domainFeeRateOfFoundation, string _domainName, bool _isOpened)
            public
            ownerExists(msg.sender)
            domainAddressNotExist(_domainAddress)
            returns (uint32 id)
        {
            id = domainRegistrationCount;
            domainRegistrations[id] = DomainRegistration({
                domainAddress : _domainAddress,
                domainFeeRateOfFoundation : _domainFeeRateOfFoundation,
                domainName : _domainName,
                isOpened : _isOpened,
                executed : false
            });

            domainRegistrationCount += 1;

            emit DomainRegistrationSubmission(id, _domainAddress, _domainFeeRateOfFoundation, _domainName, _isOpened);

            confirmDomainRegistration(id);
        }

        function confirmDomainRegistration(uint32 _id)
            public
            ownerExists(msg.sender)
            domainRegistrationExist(_id)
            notConfirmedDomainRegistration(_id, msg.sender)
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
            public
            ownerExists(msg.sender)
            confirmedDomainRegistration(_id, msg.sender)
            notExecutedDomainRegistration(_id)
        {
            if(isDomainRegistrationConfirmed(_id)) {
                DomainRegistration storage domainRegistration = domainRegistrations[_id];

                DomainConfig memory domainConfig = DomainConfig({
                    domainAddress : domainRegistration.domainAddress,
                    domainName : domainRegistration.domainName,
                    isOpened : domainRegistration.isOpened,
                    domainFeeRateOfFoundation : domainRegistration.domainFeeRateOfFoundation
                });

                domainRegistration.executed = true;


                domainConfigs[domainRegistration.domainAddress] = domainConfig;
                isDomainRegistered[domainRegistration.domainAddress] = true;
                domainCount += 1;

                emit DomainRegistrationExecution(_id);
            }
        }

        function isDomainRegistrationConfirmed (uint32 _id)
            public
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

            domainRegistrationCount += 1;

            emit DefaultFeeChangeSubmission(id, _defaultFee);

            confirmDefaultFeeChange(id);
        }

        function confirmDefaultFeeChange(uint32 _id)
            public
            ownerExists(msg.sender)
            defaultFeeChangeExist(_id)
            notConfirmedDefaultFeeChange(_id, msg.sender)
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






        //------------------------------------------------------------
        // MultiSig : Foundation account change process
        //------------------------------------------------------------

        function registerFoundationAccountChange(address _foundationAccount)
            public
            ownerExists(msg.sender)
            returns (uint32 id)
        {
            id = foundationAccountChangeCount;
            foundationAccountChanges[id] = FoundationAccountChange({
                foundationAccount : _foundationAccount,
                executed : false
            });

            foundationAccountChangeCount += 1;

            emit FoundationAccountChangeSubmission(id, _foundationAccount);

            confirmFoundationAccountChange(id);
        }

        function confirmFoundationAccountChange(uint32 _id)
            public
            ownerExists(msg.sender)
            foundationAccountChangeExist(_id)
            notConfirmedFoundationAccountChange(_id, msg.sender)
        {
            foundationAccountChangeConfirms[_id][msg.sender] = true;

            emit FoundationAccountChangeConfirmation(_id);

            executeFoundationAccountChange(_id);
        }

        function revokeFoundationAccountChangeConfirmation (uint32 _id)
            public
            ownerExists(msg.sender)
            confirmedFoundationAccountChange(_id, msg.sender)
            notExecutedFoundationAccountChange(_id)
        {
            foundationAccountChangeConfirms[_id][msg.sender] = false;

            emit FoundationAccountChangeRevocation(_id);
        }

        function executeFoundationAccountChange (uint32 _id)
            public
            ownerExists(msg.sender)
            confirmedFoundationAccountChange(_id, msg.sender)
            notExecutedFoundationAccountChange(_id)
        {
            if(isFoundationAccountChangeConfirmed(_id)) {
                FoundationAccountChange storage foundationAccountChange = foundationAccountChanges[_id];

                foundationAccountChange.executed = true;

                foundationAccount = foundationAccountChange.foundationAccount;

                emit FoundationAccountChangeExecution(_id);
            }
        }

        function isFoundationAccountChangeConfirmed (uint32 _id)
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






        //------------------------------------------------------------
        // MultiSig : Domain State change process
        //------------------------------------------------------------

        function registerDomainStateChange(uint _domainId, bool _isOpened)
            public
            ownerExists(msg.sender)
            returns (uint32 id)
        {
            id = domainStateChangeCount;
            domainStateChanges[id] = DomainStateChange({
                registered : true,
                domainId : _domainId,
                isOpened : _isOpened,
                executed : false
            });

            domainStateChangeCount += 1;

            emit DomainStateChangeSubmission(id, _domainId, _isOpened);

            confirmDomainStateChange(id);
        }

        function confirmDomainStateChange(uint32 _id)
            public
            ownerExists(msg.sender)
            domainStateChangeExist(_id)
            notConfirmedDomainStateChange(_id, msg.sender)
        {
            domainStateChangeConfirms[_id][msg.sender] = true;

            emit DomainStateChangeConfirmation(_id);

            executeDomainStateChange(_id);
        }

        function revokeDomainStateChangeConfirmation (uint32 _id)
            public
            ownerExists(msg.sender)
            confirmedDomainStateChange(_id, msg.sender)
            notExecutedDomainStateChange(_id)
        {
            domainStateChangeConfirms[_id][msg.sender] = false;

            emit DomainStateChangeRevocation(_id);
        }

        function executeDomainStateChange (uint32 _id)
            public
            ownerExists(msg.sender)
            confirmedDomainStateChange(_id, msg.sender)
            notExecutedDomainStateChange(_id)
        {
            if(isDomainStateChangeConfirmed(_id)) {
                DomainStateChange storage domainStateChange = domainStateChanges[_id];

                domainStateChange.executed = true;

                domainConfigs[domainContractAddresses[domainStateChange.domainId]].isOpened = domainStateChange.isOpened;

                emit DomainStateChangeExecution(_id);
            }
        }

        function isDomainStateChangeConfirmed (uint32 _id)
            public
            constant
            returns (bool)
        {
            uint32 count = 0;
            for (uint i = 0; i < owners.length; i++) {
                if (domainStateChangeConfirms[_id][owners[i]])
                    count += 1;
                if (count == required)
                    return true;
            }
        }






        //------------------------------------------------------------
        // MultiSig : Domain fee rate of APIS foundation change process
        //------------------------------------------------------------

        function registerDomainFeeRateChange(uint _domainId, uint _domainFeeRateOfFoundation)
            public
            ownerExists(msg.sender)
            returns (uint32 id)
        {
            id = domainFeeRateChangeCount;
            domainFeeRateChanges[id] = DomainFeeRateChange({
                registered : true,
                domainId : _domainId,
                feeRate : _domainFeeRateOfFoundation,
                executed : false
            });

            domainFeeRateChangeCount += 1;

            emit DomainFeeRateChangeSubmission(id, _domainId, _domainFeeRateOfFoundation);

            confirmDomainFeeRateChange(id);
        }

        function confirmDomainFeeRateChange(uint32 _id)
            public
            ownerExists(msg.sender)
            domainFeeRateChangeExist(_id)
            notConfirmedDomainFeeRateChange(_id, msg.sender)
        {
            domainFeeRateChangeConfirms[_id][msg.sender] = true;

            emit DomainFeeRateChangeConfirmation(_id);

            executeDomainFeeRateChange(_id);
        }

        function revokeDomainFeeRateChangeConfirmation (uint32 _id)
            public
            ownerExists(msg.sender)
            confirmedDomainFeeRateChange(_id, msg.sender)
            notExecutedDomainFeeRateChange(_id)
        {
            domainFeeRateChangeConfirms[_id][msg.sender] = false;

            emit DomainFeeRateChangeRevocation(_id);
        }

        function executeDomainFeeRateChange (uint32 _id)
            public
            ownerExists(msg.sender)
            confirmedDomainFeeRateChange(_id, msg.sender)
            notExecutedDomainFeeRateChange(_id)
        {
            if(isDomainFeeRateChangeConfirmed(_id)) {
                DomainFeeRateChange storage domainFeeRateChange = domainFeeRateChanges[_id];

                domainFeeRateChange.executed = true;

                domainConfigs[domainContractAddresses[domainFeeRateChange.domainId]].domainFeeRateOfFoundation = domainFeeRateChange.feeRate;

                emit DomainFeeRateChangeExecution(_id);
            }
        }

        function isDomainFeeRateChangeConfirmed (uint32 _id)
            public
            constant
            returns (bool)
        {
            uint32 count = 0;
            for (uint i = 0; i < owners.length; i++) {
                if (domainStateChangeConfirms[_id][owners[i]])
                    count += 1;
                if (count == required)
                    return true;
            }
        }






        function registerWithdrawal(address _receiver)
        public
        ownerExists(msg.sender)
        validWithdrawalAmount
        returns (uint id)
        {
            id = withdrawalCount;
            withdrawals[id] = Withdrawal({
                registered : true,
                aApis : address(this).balance,
                receiver : _receiver,
                executed : false
                });

            withdrawalCount += 1;

            emit WithdrawalSubmission(id, address(this).balance);

            confirmWithdrawal(id);
        }

        function confirmWithdrawal(uint _id)
        public
        ownerExists(msg.sender)
        withdrawalExist(_id)
        notConfirmedWithdrawal(_id, msg.sender)
        {
            withdrawalConfirms[_id][msg.sender] = true;

            emit WithdrawalConfirmation(_id);

            executeWithdrawal(_id);
        }

        function revokeWithdrawalConfirmation (uint _id)
        public
        ownerExists(msg.sender)
        confirmedWithdrawal(_id, msg.sender)
        notExecutedWithdrawal(_id)
        {
            withdrawalConfirms[_id][msg.sender] = false;

            emit WithdrawalRevocation(_id);
        }

        function executeWithdrawal (uint _id)
        public
        ownerExists(msg.sender)
        confirmedWithdrawal(_id, msg.sender)
        notExecutedWithdrawal(_id)
        {
            if(isWithdrawalConfirmed(_id)) {
                Withdrawal storage withdrawal = withdrawals[_id];

                require(withdrawal.aApis <= address(this).balance);

                address receiver = withdrawal.receiver;
                receiver.transfer(withdrawal.aApis);

                emit WithdrawalExecution(_id, withdrawal.aApis);
            }
        }

        function isWithdrawalConfirmed (uint _id)
        public
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
    }