document.addEventListener("DOMContentLoaded", function() {
    const currentDate = new Date();
    let currentMonth = currentDate.getMonth();
    let currentYear = currentDate.getFullYear();

    const monthNames = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ];

    const monthElement = document.getElementById("currentMonth");

    function updateCalendar() {
        // Clear previous calendar
        const calendarGrid = document.querySelector(".calendar-grid");
        calendarGrid.innerHTML = "";

        // Display month and year
        monthElement.textContent = `${monthNames[currentMonth]} ${currentYear}`;

        // Calculate the first day of the month
        const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
        const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

        // Generate days of the week headers
        const daysOfWeek = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
        daysOfWeek.forEach(day => {
            const dayName = document.createElement("div");
            dayName.classList.add("day-name");
            dayName.textContent = day;
            calendarGrid.appendChild(dayName);
        });

        // Generate days of the month
        let dayIndex = 0;
        for (let i = 0; i < 6; i++) { // Assume maximum 6 weeks for display
            for (let j = 0; j < 7; j++) {
                const calendarSquare = document.createElement("div");
                calendarSquare.classList.add("calendar-square");

                if (i === 0 && j < firstDayOfMonth) {
                    // Empty square before the first day of the month
                    calendarSquare.textContent = "";
                } else {
                    dayIndex++;
                    if (dayIndex > daysInMonth) {
                        // No more days in the month, leave square empty
                        calendarSquare.textContent = "";
                    } else {
                        calendarSquare.textContent = dayIndex;
                    }
                }

                calendarGrid.appendChild(calendarSquare);
            }
        }
        populate();
    }

    function populate() {
        var datelist = document.getElementsByClassName("date");
        var starthourlist = document.getElementsByClassName("starthour");
        var startminutelist = document.getElementsByClassName("startminute");
        var endhourlist = document.getElementsByClassName("endhour");
        var endminutelist = document.getElementsByClassName("endminute");
        var dayweeklist = document.getElementsByClassName("dayweek");
        var weeklylist = document.getElementsByClassName("weekly");
        console.log(datelist[0]);
        const daysOfWeek = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
        var days = document.getElementsByClassName("calendar-square");
        for (let x = 0; x < days.length; x++)
        {
            var dayName = x % 7; //dayName = 0 means Sunday, etc
            for (let i = 0; i < datelist.length; i++)
            {
                if (days[x].innerHTML != "" && weeklylist[i].innerHTML == "true" && dayweeklist[i].innerHTML == daysOfWeek[dayName])
                {
                    const eventElement = document.createElement("div");
                    eventElement.classList.add("event");
                    eventElement.textContent = starthourlist[i].innerHTML + ":" + startminutelist[i].innerHTML + " - " +
                    endhourlist[i].innerHTML + ":" + endminutelist[i].innerHTML;
                    days[x].innerHTML = days[x].innerHTML + "<br>Shift:";
                    days[x].appendChild(eventElement);
                }

                else if (weeklylist[i].innerHTML == "false" && datelist[i].innerHTML == String(currentYear) + "-" + String(currentMonth+1).padStart(2, '0') + "-" + days[x].innerHTML.padStart(2, '0'))
                {
                    const eventElement = document.createElement("div");
                    eventElement.classList.add("event");
                    eventElement.textContent = starthourlist[i].innerHTML + ":" + startminutelist[i].innerHTML + " - " +
                    endhourlist[i].innerHTML + ":" + endminutelist[i].innerHTML;
                    days[x].innerHTML = days[x].innerHTML + "<br>Shift:";
                    days[x].appendChild(eventElement);
                }
            }
        }
    }

    updateCalendar();

    // Handle month navigation
    document.getElementById("prevMonth").addEventListener("click", function() {
        if (currentMonth > 0) {
            currentMonth--;
        } else {
            currentMonth = 11;
            currentYear--;
        }
        updateCalendar();
    });

    document.getElementById("nextMonth").addEventListener("click", function() {
        if (currentMonth < 11) {
            currentMonth++;
        } else {
            currentMonth = 0;
            currentYear++;
        }
        updateCalendar();
    });
});

function validateAndSubmit() {
    const year = document.getElementById("year");
    const month = document.getElementById("month");
    const day = document.getElementById("day");
    const startTime = document.getElementById("startTime");
    const duration = document.getElementById("duration");

    if (!year.value || !month.value || !day.value || !startTime.value|| !duration.value) {
        alert("Please fill out all required fields (Year, Month, Day, Start Time, Duration).");
        return false;
    }
    
    // Form data is valid, proceed with submission
    else {
        document.getElementById("scheduleForm").submit();
    }
}

function deleteform(DeleteButton)
    {
        document.getElementById("deleteform").action = ("/manager/deleteschedule/" + (DeleteButton.id).toString());
        document.getElementById("deleteform").submit();
    }