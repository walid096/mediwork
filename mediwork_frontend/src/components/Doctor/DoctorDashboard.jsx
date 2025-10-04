import React from 'react';
import DoctorCalendar from './DoctorCalendar';
import DoctorInfoCard from './DoctorInfoCard';
import DailyHealthTip from './DailyHealthTip';


const DoctorDashboard = () => {
    return (
        <div className='flex flex-col md:flex-row gap-6'>
        <div>
            <DoctorInfoCard />
            <DoctorCalendar />
        </div>
            <DailyHealthTip />

        </div>
    );
};

export default DoctorDashboard;
