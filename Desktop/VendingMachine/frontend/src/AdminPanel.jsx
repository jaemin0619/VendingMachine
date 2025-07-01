import React, { useState, useEffect, useRef } from 'react';
import './AdminPanel.css';

function AdminPanel({ drinks }) {
  const [selectedTab, setSelectedTab] = useState('menu');
  const [editInfo, setEditInfo] = useState([...drinks]);
  const socketRef = useRef(null);

  useEffect(() => {
    const socket = new WebSocket('ws://localhost:3001');
    socketRef.current = socket;

    socket.onopen = () => {
      console.log('WebSocket 연결 성공');
      // 연결 후 재고 요청
      socket.send(JSON.stringify({ type: 'getInventory' }));
    };

    socket.onmessage = (e) => {
      try {
        const msg = JSON.parse(e.data);

        if (msg.type === 'salesData') {
          const list = msg.data.map(entry => {
            if (msg.salesType === 'total') {
              return `총 매출: ${entry.total}원`;
            } else if (msg.salesType === 'daily') {
              return `${entry.date} : ${entry.total}원`;
            } else if (msg.salesType === 'monthly') {
              return `${entry.month} : ${entry.total}원`;
            }
            return '';
          }).join('\n');
          alert(`📊 ${msg.salesType} 매출\n` + list);

        } else if (msg.type === 'inventory') {
          setEditInfo(msg.data); // 전체 재고 동기화
        } else {
          alert(`📨 서버 응답: ${msg.message || e.data}`);
        }
      } catch {
        alert('📨 서버 응답: ' + e.data);
      }
    };

    socket.onerror = (e) => {
      console.error('WebSocket 오류:', e);
    };

    socket.onclose = () => {
      console.log('WebSocket 연결 종료됨');
    };

    return () => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        console.log("WebSocket 클린업");
        socket.close();
      }
    };
  }, []);

  const sendMessage = (type, data = {}) => {
    const socket = socketRef.current;
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ type, ...data }));
    } else {
      alert('서버에 연결되어 있지 않습니다.');
    }
  };

  const handlePriceChange = (index, field, value) => {
    const updated = [...editInfo];
    updated[index] = { ...updated[index], [field]: value };
    setEditInfo(updated);
  };

  return (
    <div className="admin-panel">
      <h2>관리자 패널 (WebSocket 연결)</h2>

      <div className="tab-buttons">
        <button onClick={() => setSelectedTab('menu')}>메뉴 수정</button>
        <button onClick={() => setSelectedTab('restock')}>재고 보충</button>
        <button onClick={() => setSelectedTab('sales')}>매출 보기</button>
        <button onClick={() => sendMessage('collect')}>수금하기</button>
        <button onClick={() => sendMessage('sendLog')}>서버 전송</button>
        <button onClick={() => {
          const newPw = prompt("🔑 새 비밀번호를 입력하세요:");
          if (newPw) sendMessage('changePassword', { newPassword: newPw });
        }}>비밀번호 변경</button>
      </div>

      {selectedTab === 'menu' && (
        <div className="edit-menu">
          {editInfo.map((drink, i) => (
            <div key={i} className="drink-row">
              <input
                value={drink.name}
                onChange={(e) => handlePriceChange(i, 'name', e.target.value)}
              />
              <input
                type="number"
                value={drink.price}
                onChange={(e) => handlePriceChange(i, 'price', Number(e.target.value))}
              />
              <button onClick={() => sendMessage('edit', { id: i, ...editInfo[i] })}>
                저장
              </button>
            </div>
          ))}
        </div>
      )}

      {selectedTab === 'restock' && (
        <div className="restock-menu">
          {editInfo.map((drink, i) => (
            <div key={i} className="drink-row">
              <span>{drink.name} (재고: {drink.stock})</span>
              <input
                type="number"
                placeholder="보충 수량"
                onBlur={(e) => {
                  const amount = Number(e.target.value);
                  if (amount > 0) sendMessage('restock', { id: i, amount });
                }}
              />
            </div>
          ))}
        </div>
      )}

      {selectedTab === 'sales' && (
        <div className="sales-menu">
          <button onClick={() => sendMessage('viewSales', { viewType: 'daily' })}>📅 일별 매출</button>
          <button onClick={() => sendMessage('viewSales', { viewType: 'monthly' })}>📆 월별 매출</button>
          <button onClick={() => sendMessage('viewSales', { viewType: 'total' })}>📊 전체 매출</button>
        </div>
      )}
    </div>
  );
}

export default AdminPanel;
