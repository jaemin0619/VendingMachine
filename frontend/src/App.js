// App.js
import React, { useState } from 'react';
import './App.css';
import AdminPanel from './AdminPanel';

function App() {
  const [drinks, setDrinks] = useState([
    { name: '믹스커피', price: 200, stock: 10 },
    { name: '고급믹스', price: 300, stock: 10 },
    { name: '물', price: 450, stock: 10 },
    { name: '캔커피', price: 500, stock: 10 },
    { name: '이온음료', price: 550, stock: 10 },
    { name: '고급캔커피', price: 700, stock: 10 },
    { name: '탄산음료', price: 750, stock: 10 },
    { name: '특화음료', price: 800, stock: 10 }
  ]);

  const handleRestock = (index, amount) => {
    if (amount > 0) {
      const newDrinks = [...drinks];
      newDrinks[index].stock += amount;
      setDrinks(newDrinks);
      alert(`${newDrinks[index].name} 재고 ${amount}개 보충 완료`);
    }
  };

  const handleEdit = (index, newInfo) => {
    const newDrinks = [...drinks];
    newDrinks[index] = newInfo;
    setDrinks(newDrinks);
    alert(`${newInfo.name} 정보 수정 완료`);
  };

  const handleCollect = () => {
    alert("💸 수금 완료 (더미 기능)");
  };

  const handleSendLog = () => {
    alert("📡 서버로 로그 전송 완료 (더미 기능)");
  };

  const handleChangePassword = () => {
    alert("🔐 비밀번호 변경 (더미 기능)");
  };

  const handleViewSales = (type) => {
    alert(`📊 ${type} 매출 보기 (더미 기능)`);
  };

  return (
    <div className="App">
      <AdminPanel
        drinks={drinks}
        onRestock={handleRestock}
        onEdit={handleEdit}
        onCollect={handleCollect}
        onSendLog={handleSendLog}
        onChangePassword={handleChangePassword}
        onViewSales={handleViewSales}
      />
    </div>
  );
}

export default App;
