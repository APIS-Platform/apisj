pragma solidity ^0.4.18;

/**
 * @dev 개인키 외에 비밀번호를 이용해서 트랜잭션을 서명할 수 있는 2FA 기능을 제공한다.
 */
contract ProofOfKnowledge {

    event RegisterProofKey (address proofKey);
    event RemoveProofKey ();

    modifier notEmptyKey (address key) {
        require(key != 0x0);
        _;
    }

    function ()
    payable
    public {
        revert();
    }

    /**
     * @dev ProofKey를 등록한다.
     *      이 메서드가 실행되어 RegisterProofKey 이벤트가 발생할 경우,
     *      AccountState의 ProofKey 값을 변경한다.
     * @param _key 주소 형식의 proofKey
     */
    function registerProofKey (address _key)
    public
    notEmptyKey(_key)
    {
        emit RegisterProofKey(_key);
    }

    /**
     * @dev ProofKey를 제거한다.
     *      이 메서드가 실행되어 RemoveProofKey 이벤트가 발생할 경우,
     *      AccountState의 Proofkey 값을 삭제한다.
     */
    function removeProofKey ()
    public
    {
        emit RemoveProofKey();
    }
}