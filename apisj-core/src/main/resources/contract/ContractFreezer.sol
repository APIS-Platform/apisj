pragma solidity ^0.4.18;


/**
 * @dev 스마트 컨트렉트 코드를 변경할 수 없도록 고정하는(얼리는) 기능을 수행한다.
 *      얼리려는 컨트렉트는 직접 이 컨트렉트의 fallback 함수를 호출해야 한다.
 */
contract ContractFreezer {
    
    event Frozen (address target);
    
    mapping(address => bool) glaciers;
    
    
    constructor () public {}
    
    function () public payable {
        if(msg.value > 0) {
            address(msg.sender).transfer(msg.value);
        }
        
        glaciers[msg.sender] = true;
        
        emit Frozen(msg.sender);
    }
    
    function isFrozen(address addr) public view returns (bool) {
        return glaciers[addr];
    }
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
    // @dev 이 함수와 같이 freezer를 호출하면 앞으로는 컨트렉트를 수정할 수 없게 된다.
    function freezeThisContract() public {
        require(freezer.call(0));
    }
}