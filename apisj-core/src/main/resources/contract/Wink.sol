pragma solidity ^0.4.24;

contract WinkTest {
    /**
     * @dev 컨트렉트에서 수수료를 부담하도록 하기 위한 이벤트
     */
    event Wink (address beneficiary, address winker);

    uint256 foo;

    function test()
    public
    {
        foo += 1;

        emit Wink(msg.sender, address(this));
    }
}