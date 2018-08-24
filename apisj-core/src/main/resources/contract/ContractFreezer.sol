pragma solidity ^0.4.18;


/**
 * @dev 스마트 컨트렉트 코드를 변경할 수 없도록 고정하는(얼리는) 기능을 수행한다.
 *      얼리려는 컨트렉트는 직접 이 컨트렉트의 fallback 함수를 호출해야 한다.
 */
contract ContractFreezer {

    event Frozen (address target);

    mapping(address => bool) glaciers;

    //@dev Filtering is required to freeze only the fresh contract.
    modifier onlyFresh() {
        require(glaciers[msg.sender] == false);
        _;
    }

    //@dev Funds(APIS) can not be transferred to this contract.
    modifier onlyWithoutPenny() {
        require(msg.value == 0);
        _;
    }

    //@dev Executing the Fallback function makes it impossible to modify the code.
    function () public payable onlyFresh onlyWithoutPenny {
        glaciers[msg.sender] = true;
        emit Frozen(msg.sender);
    }

    function isFrozen(address addr) public view returns (bool) {
        return glaciers[addr];
    }

    constructor () public {}
}


/**
 * @dev Example of freezing code
 */
contract Tester {
    address public freezer;

    constructor (address _freezer) public {
        freezer = _freezer;
    }

    // @dev If you freeze contract like this function, you will not be able to modify the contract in the future.
    function freezeThisContract() public {
        require(freezer.call(0));
    }
}